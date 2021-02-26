package org.matrixer.agent.util;

import static org.junit.jupiter.api.Assertions.*;

public class Assertions {

    public static void assertFoundTestCase(String output, String caller, String callee) {
        assertTrue(output.contains(caller),
                "\noutput: '" + output + "'\nDid not contain\n'" + caller + "'\n");
        assertTrue(output.contains(callee),
                "\noutput: '" + output + "'\nDid not contain\n'" + callee + "'\n");
    }
}
