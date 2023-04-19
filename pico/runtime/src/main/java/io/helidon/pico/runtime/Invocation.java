/*
 * Copyright (c) 2023 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.pico.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Function;

import io.helidon.pico.api.Interceptor;
import io.helidon.pico.api.InvocationContext;
import io.helidon.pico.api.ServiceProvider;

import jakarta.inject.Provider;

/**
 * Handles the invocation of {@link Interceptor} methods.
 *
 * @see io.helidon.pico.api.InvocationContext
 * @param <V> the invocation type
 */
public class Invocation<V> implements Interceptor.Chain<V> {
    private final InvocationContext ctx;
    private final ListIterator<Provider<Interceptor>> interceptorIterator;
    private Function<Object[], V> call;

    private Invocation(InvocationContext ctx,
                       Function<Object[], V> call) {
        this.ctx = ctx;
        this.call = Objects.requireNonNull(call);
        this.interceptorIterator = ctx.interceptors().listIterator();
    }

    @Override
    public String toString() {
        return String.valueOf(ctx.elementInfo());
    }

    /**
     * Creates an instance of {@link Invocation} and invokes it in this context.
     *
     * @param ctx   the invocation context
     * @param call  the call to the base service provider's method
     * @param args  the call arguments
     * @param <V>   the type returned from the method element
     * @return the invocation instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <V> V createInvokeAndSupply(InvocationContext ctx,
                                              Function<Object[], V> call,
                                              Object[] args) {
        if (ctx.interceptors().isEmpty()) {
            return call.apply(args);
        } else {
            return (V) new Invocation(ctx, call).proceed(args);
        }
    }

    /**
     * Merges a variable number of lists together, where the net result is the merged set of non-null providers
     * ranked in proper weight order, or else empty list.
     *
     * @param lists the lists to merge
     * @param <T>   the type of the provider
     * @return the merged result, or null instead of empty lists
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Provider<T>> mergeAndCollapse(List<Provider<T>>... lists) {
        List<Provider<T>> result = null;

        for (List<Provider<T>> list : lists) {
            if (list == null) {
                continue;
            }

            for (Provider<T> p : list) {
                if (p == null) {
                    continue;
                }

                if (p instanceof ServiceProvider
                        && VoidServiceProvider.serviceTypeName().equals(
                                ((ServiceProvider<?>) p).serviceInfo().serviceTypeName())) {
                    continue;
                }

                if (result == null) {
                    result = new ArrayList<>();
                }
                if (!result.contains(p)) {
                    result.add(p);
                }
            }
        }

        if (result != null && result.size() > 1) {
            result.sort(DefaultServices.serviceProviderComparator());
        }

        return (result != null) ? Collections.unmodifiableList(result) : List.of();
    }

    @Override
    public V proceed(Object... args) {
        if (interceptorIterator.hasNext()) {
            return interceptorIterator.next()
                    .get()
                    .proceed(ctx, this, args);
        } else {
            if (this.call != null) {
                Function<Object[], V> call = this.call;
                this.call = null;
                return call.apply(args);
            } else {
                throw new IllegalStateException("Duplicate invocation, or unknown call type: " + this);
            }
        }
    }

}