package org.matrixer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectPreparer {

    private Path gradleBuildFile;
    private Path mavenBuildFile;

    private Properties prop;

    public ProjectPreparer(Properties p) {
        this.prop = p;
    }

    void prepare() {
        if (!searchForBuildFiles(prop.targetPath())) {
            throw new RuntimeException("Failed to prepare target project: No build file found");
        }
        if (gradleBuildFile != null) {
            prepareGradleBuildFile();
        }
    }

    private boolean searchForBuildFiles(Path projectPath) {
        String GRADLE_BUILD_FILE_NAME = "build.gradle";
        var results = FileUtils.fileSearch(projectPath, GRADLE_BUILD_FILE_NAME);
        if (results.length > 0) {
            gradleBuildFile = results[0];
            return true;
        }
        String MAVEN_BUILD_FILE_NAME = "pom.xml";
        results = FileUtils.fileSearch(projectPath, MAVEN_BUILD_FILE_NAME);
        if (results.length > 0) {
            mavenBuildFile = results[0];
            return true;
        }
        return false;
    }

    private void prepareGradleBuildFile() {
        String injectString = createInjectString();
        String regex = "test \\{";
        FileUtils.replaceFirstOccurrenceInFile(gradleBuildFile, regex, injectString);
    }

    private String createInjectString() {
        return "test {\n\tjvmArgs \"-javaagent:" + resolvePathToAgent() + "="
            + prop.outputPath() + ":org.matrixertest\"";
    }

    private String resolvePathToAgent() {
        Path root = Path.of(System.getProperty("user.dir")).getParent();
        Path relative = Paths.get("agent", "build", "libs", "agentJar.jar");
        return  root.resolve(relative).toString();
    }

    public Path getGradleBuildFile() {
        return gradleBuildFile;
    }

    public Path getMavenBuildFile() {
        return mavenBuildFile;
    }
}
