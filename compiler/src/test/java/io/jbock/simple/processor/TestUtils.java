/*
 * Copyright (C) 2018 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jbock.simple.processor;

import java.util.List;

final class TestUtils {

    /**
     * Returns the lines joined by {@code "\n  "}. Useful for passing to {@link
     * io.jbock.testing.compile.CompilationSubject#hadErrorContaining(String)}
     */
    static String message(String... lines) {
        return String.join("\n  ", List.of(lines));
    }
}
