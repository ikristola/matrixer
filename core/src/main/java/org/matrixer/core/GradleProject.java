package org.matrixer.core;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

class GradleProject extends Project {

    public static final String scriptName = "build.gradle";
    public static final String buildDirName = "build";

    private Path outputDir;

    GradleProject(Properties properties) {
        super(properties);
        setOutputDirOrDefault(properties.outputDir());
    }

    private void setOutputDirOrDefault(Path dir) {
        if (dir != null) {
            this.outputDir = dir;
            return;
        }
        setDefaultOutputDirectory();
    }

    private void setDefaultOutputDirectory() {
        this.outputDir = directory().resolve(Path.of(buildDirName, OUTPUT_DIR_NAME));
    }

    @Override
    public Path buildScript() {
        return directory().resolve(scriptName);
    }

    @Override
    public Path outputDirectory() {
        return outputDir;
    }

    @Override
    void injectBuildScript(String agentString) {
        String injectString = createInjectString(agentString);
        FileUtils.appendToFile(buildScript(), injectString);
    }

    private String createInjectString(String agentString) {
        return "\ntasks.withType(Test) {\n\tjvmArgs \"" + agentString + "\"\n}\n";
    }

    @Override
    List<String> getTestCommand() {
        String[] cmd = new String[] {"./gradlew", "test"};
        return Arrays.asList(cmd);
    }

    @Override
    public String targetPackage() {
        if (properties.targetPackage() == null) {
            throw new RuntimeException("Target package must be specified for gradle projects");
        }
        return properties.targetPackage();
    }

    @Override
    public String testPackage() {
        if (properties.testPackage() == null) {
            return targetPackage();
        }
        return properties.testPackage();
    }
}
