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
package org.matrixer.agent.util;

import static org.junit.jupiter.api.Assertions.*;

public class Assertions {

    public static void assertFoundTestCase(String output, String caller, String callee) {

        assertTrue(output.contains(callee),
                "\noutput: '" + output + "'\nDid not contain\n'" + callee + "'\n");
        assertTrue(output.contains(caller),
                "\noutput: '" + output + "'\nDid not contain\n'" + caller + "'\n");

        int idx = output.indexOf('|');
        assertNotEquals(-1, idx, "No depth recorded: '" + output + "'");
        String depthString = output.substring(0, idx);
        assertIsInteger(depthString);
    }

    public static void assertIsInteger(String str) {
        assertTrue(isInteger(str), "Not an integer " + str);
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
