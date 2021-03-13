package org.matrixer;

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
    Path buildScript() {
        return directory().resolve(scriptName);
    }

    @Override
    Path outputDirectory() {
        return outputDir;
    }

    @Override
    void injectBuildScript(String agentString) {
        String injectString = createInjectString(agentString);
        String regex = "test \\{";
        FileUtils.replaceFirstOccurrenceInFile(buildScript(), regex, injectString);
    }

    private String createInjectString(String agentString) {
        return "test {\n\tjvmArgs \"" + agentString + "\"";
    }

    @Override
    List<String> getTestCommand() {
        String[] cmd = new String[] { "./gradlew", "test" };
        return Arrays.asList(cmd);
    }
}
