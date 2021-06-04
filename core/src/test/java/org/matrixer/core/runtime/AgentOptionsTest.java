package org.matrixer.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AgentOptionsTest {

    private static Path defaultJar = Path.of("matrixerAgent.jar");

    @Test
    void testDefaults() {
        AgentOptions options = new AgentOptions();
        assertEquals(AgentOptions.DEFAULT_DESTFILENAME, options.getDestFilename());
        assertEquals("", options.getTargetPackage());
        assertEquals("", options.getTestPackage());
        assertEquals(false, options.getDebug());
        assertEquals(0, options.getDepthLimit());

        assertEquals("", options.toString());
    }

    @Test
    void testEmpty() {
        AgentOptions options = new AgentOptions("");
        assertEquals("", options.toString());
    }

    @Test
    void testNull() {
        AgentOptions options = new AgentOptions((String) null);
        assertEquals("", options.toString());
    }

    @Test
    void testSetDebug() {
        AgentOptions options = new AgentOptions();
        options.setDebug(true);
        assertEquals(true, options.getDebug());
    }

    @Test
    void testGetDebug() {
        AgentOptions options = new AgentOptions("debug=true");
        assertEquals(true, options.getDebug());
    }

    @Test
    void testSetDestfile() {
        AgentOptions options = new AgentOptions();
        String destFilename = "matrixer-cov.txt";
        options.setDestFilename(destFilename);
        assertEquals(destFilename, options.getDestFilename());
    }

    @Test
    void testGetDestfile() {
        AgentOptions options = new AgentOptions("destfile=matrixer-cov.txt");
        assertEquals("matrixer-cov.txt", options.getDestFilename());
    }

    @Test
    void testSetTargetPackage() {
        AgentOptions options = new AgentOptions();
        String targetPackage = "org.matrixer";
        options.setTargetPackage(targetPackage);
        assertEquals(targetPackage, options.getTargetPackage());
    }

    @Test
    void testGetTargetPackage() {
        AgentOptions options = new AgentOptions("pkg=org.matrixer");
        assertEquals("org.matrixer", options.getTargetPackage());
    }

    @Test
    void testSetTestPackage() {
        AgentOptions options = new AgentOptions();
        String testPackage = "org.matrixer.test";
        options.setTestPackage(testPackage);
        assertEquals(testPackage, options.getTestPackage());
    }

    @Test
    void testGetTestPackage() {
        AgentOptions options = new AgentOptions("testPkg=org.matrixer");
        assertEquals("org.matrixer", options.getTestPackage());
    }

    @Test
    void testGetDepthLimit() {
        AgentOptions options = new AgentOptions("depth=42");
        assertEquals(42, options.getDepthLimit());
    }

    @Test
    void testSetDepthLimit() {
        AgentOptions options = new AgentOptions();
        int depth = 142;
        options.setDepthLimit(depth);
        assertEquals(depth, options.getDepthLimit());
    }

    @Test
    void testInvalidOption() {
        assertThrows(IllegalArgumentException.class, () -> new AgentOptions("eggs=true"));
    }

    @Test
    void invalidOptionFormat() {
        assertThrows(IllegalArgumentException.class, () -> new AgentOptions("debug"));
    }

    @Test
    void testToStringSingleOption() {
        AgentOptions options = new AgentOptions();
        options.setTestPackage("org.matrixer");
        assertEquals("testPkg=org.matrixer", options.toString());
    }

    @Test
    void testToStringMultipleOption() {
        AgentOptions options = new AgentOptions();
        options.setDestFilename("matrixer-cov.txt");
        options.setTargetPackage("org.matrixer");
        options.setTestPackage("org.matrixer.test");
        options.setDebug(true);
        assertEquals("destfile=matrixer-cov.txt,pkg=org.matrixer,testPkg=org.matrixer.test,debug=true",
                options.toString());
    }

    @Test
    void testJVMArgumentsMultipleOptions() {
        AgentOptions options = new AgentOptions();
        options.setDestFilename("matrixer-cov.txt");
        options.setTargetPackage("org.matrixer");
        options.setTestPackage("org.matrixer.test");
        options.setDebug(true);
        assertEquals(
                String.format("-javaagent:%s=%s", defaultJar, options.toString()),
                    options.getJVMArgument(defaultJar));
    }

    @Test
    void testJVMArgumentsNoOptions() {
        AgentOptions options = new AgentOptions();
        assertEquals(
                String.format("-javaagent:%s=", defaultJar), options.getJVMArgument(defaultJar));
    }

}
