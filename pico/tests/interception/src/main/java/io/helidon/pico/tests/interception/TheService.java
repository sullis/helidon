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

package io.helidon.pico.tests.interception;

import jakarta.inject.Singleton;

@Singleton
class TheService {
    @Modify
    TheService() {
    }

    // args:
    // 0: String message
    // 1: Boolean modify
    // 2: Boolean repeat
    // 3: Boolean return
    @Modify
    @Repeat
    @Return
    String intercepted(String message, boolean modify, boolean repeat, boolean doReturn) {
        return message;
    }

    @Repeat
    @Return
    String interceptedSubset(String message, boolean modify, boolean repeat, boolean doReturn) {
        return message;
    }

    String notIntercepted(String message, boolean modify, boolean repeat, boolean doReturn) {
        return message;
    }
}