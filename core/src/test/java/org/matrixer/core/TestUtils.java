package org.matrixer.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

class TestUtils {

    final static String targetRootPackage = "org.matrixertest";
    final static String CWD = System.getProperty("user.dir");
    final static Path AGENT_JAR_PATH =
            Path.of(CWD, "../agent/build/libs/agentJar.jar").normalize();

    static Path targetDirectory() {
        return FileUtils.getSystemTempDir().resolve("matrixer-test");
    }

    static URI testRepoURL() {
        return asURI(testRepoURLAsString());
    }

    static String testRepoURLAsString() {
        return "https://github.com/ikristola/matrixer-test";
    }

    static void removeGradleFiles(Path projectDir) {
        var gradleFiles = new String[] {"gradle", "gradlew", "gradlew.bat", "build.gradle",
                "gradle.settings"};
        FileUtils.removeFiles(projectDir, gradleFiles);
    }

    static void removeMavenFiles(Path projectDir) {
        var gradleFiles = new String[] {"pom.xml"};
        FileUtils.removeFiles(projectDir, gradleFiles);
    }

    static URI asURI(String uriString) {
        if (uriString == null || uriString.isBlank()) {
            return null;
        }
        try {
            return new URI(uriString);
        } catch (URISyntaxException e1) {
            throw new IllegalArgumentException("Not a valid url: " + uriString);
        }
    }

    static String agentString(Path outputPath) {
        return agentString(
                AGENT_JAR_PATH, outputPath,
                targetRootPackage, targetRootPackage);
    }

    static String agentString(Path agentJarPath, Path outputPath,
            String targetPkg, String testPkg) {
        return String.format("-javaagent:%s=%s:%s:%s",
                agentJarPath.toString(), outputPath.toString(),
                targetRootPackage, targetRootPackage);
    }

}
