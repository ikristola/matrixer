package org.matrixer.core.testsupport;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.matrixer.core.util.FileUtils;

public class TestUtils {

    public final static String targetRootPackage = "org.matrixertest";
    final static String CWD = System.getProperty("user.dir");
    final static Path AGENT_JAR_PATH =
            Path.of(CWD, "../agent/build/libs/agentJar.jar").normalize();

    public static Path targetDirectory() {
        return FileUtils.getSystemTempDirectory().resolve("matrixer-test");
    }

    public static URI testRepoURL() {
        return asURI(testRepoURLAsString());
    }

    public static String testRepoURLAsString() {
        return "https://github.com/ikristola/matrixer-test";
    }

    public static void removeGradleFiles(Path projectDir) {
        String[] gradleFiles =
                {"gradle", "gradlew", "gradlew.bat", "build.gradle", "gradle.settings"};
        FileUtils.removeFiles(projectDir, gradleFiles);
    }

    public static void removeMavenFiles(Path projectDir) {
        var gradleFiles = new String[] {"pom.xml"};
        FileUtils.removeFiles(projectDir, gradleFiles);
    }

    public static URI asURI(String uriString) {
        if (uriString == null || uriString.isBlank()) {
            return null;
        }
        try {
            return new URI(uriString);
        } catch (URISyntaxException e1) {
            throw new IllegalArgumentException("Not a valid url: " + uriString);
        }
    }

    public static String agentString(Path outputPath) {
        return agentString(
                AGENT_JAR_PATH, outputPath,
                targetRootPackage, targetRootPackage);
    }

    public static String agentString(Path agentJarPath, Path outputPath,
            String targetPkg, String testPkg) {
        return String.format("-javaagent:%s=%s:%s:%s",
                agentJarPath.toString(), outputPath.toString(),
                targetPkg, testPkg);
    }

    public static InputStream asInputStream(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    public static String getString(ByteArrayOutputStream out) {
        return new String(out.toByteArray());
    }

}
