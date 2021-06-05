/**
 * Copyright 2021 Patrik Bogren, Isak Kristola
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
package org.matrixer.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MethodCallTest {

    String sep = MethodCall.sep;

    @Test
    void parseMethodCall() {
        int callStackDepth = 42;
        String methodName = "Amethod";
        String callerName = "SomeCaller";
        String line = callStackDepth + sep + methodName + sep + callerName;

        MethodCall call = new MethodCall(line);
        assertEquals(callStackDepth, call.depth, "Depth not correct");
        assertEquals(methodName, call.methodName, "Method name not correct");
        assertEquals(callerName, call.callerName, "Caller name not correct");
    }

    @Test
    void parseMethodThrowsIfDepthNotANumber() {
        String line = "42s" + sep + "method" + sep + "testcase"; 
        assertThrows(IllegalArgumentException.class, () -> new MethodCall(line));
    }

    @Test
    void parseMethodThrowsIfNotEnoughSeparators() {
        String line = "42" + sep + "method"; 
        assertThrows(IllegalArgumentException.class, () -> new MethodCall(line));
    }

    @Test
    void writeCallAsLine() {
        MethodCall expected = new MethodCall(555, "TheMethod", "TheCaller");
        String line = expected.asLine();

        MethodCall actual = new MethodCall(line);
        assertEquals(expected, actual);
    }

}
