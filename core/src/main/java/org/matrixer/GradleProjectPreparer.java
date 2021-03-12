package org.matrixer;

import java.nio.file.Path;
import java.nio.file.Paths;

class GradleProjectPreparer implements ProjectPreparer {

    public final static String BUILD_DIR_NAME = "build";

    private final Path targetDir;
    private final Path outputDir;
    private final Path buildScript;

    public GradleProjectPreparer(Path targetDir) {
        this(targetDir, Properties.defaultOutputPath(targetDir, BUILD_DIR_NAME));
    }

    public GradleProjectPreparer(Path targetDir, Path outputDir) {
        this.targetDir = targetDir;
        this.outputDir = outputDir;
        this.buildScript = targetDir.resolve(GRADLE_BUILD_FILE_NAME);
    }


    @Override
    public BuildType buildType() {
        return BuildType.Gradle;
    }

    /**
     * @returns a Path to the gradle build script of the project
     */
    @Override
    public Path getBuildScript() {
        return buildScript;
    }

    @Override
    public void prepare() {
        prepareBuildScript();
    }

    private void prepareBuildScript() {
        String injectString = createInjectString();
        String regex = "test \\{";
        FileUtils.replaceFirstOccurrenceInFile(buildScript, regex, injectString);
    }

    private String createInjectString() {
        return "test {\n\tjvmArgs \"-javaagent:" + resolvePathToAgent() + "=" + outputDir
                + ":org.matrixertest:org.matrixertest\"";
    }

    final String resolvePathToAgent() {
        Path root = Path.of(System.getProperty("user.dir")).getParent();
        Path relative = Paths.get("agent", "build", "libs", "agentJar.jar");
        return root.resolve(relative).toString();
    }

}
