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
