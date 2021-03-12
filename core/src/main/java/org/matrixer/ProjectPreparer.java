package org.matrixer;

import java.nio.file.Path;

/**
 * Prepares a target project for further processing
 */
public interface ProjectPreparer {
    final static String GRADLE_BUILD_FILE_NAME = "build.gradle";
    final static String MAVEN_BUILD_FILE_NAME = "pom.xml";

    public static ProjectPreparer scan(Properties prop) {
        return scan(prop.targetDir());
    }

    public static ProjectPreparer scan(Path targetPath) {
        var paths = FileUtils.fileSearch(targetPath, GRADLE_BUILD_FILE_NAME);
        if (paths.length > 0) {
            return new GradleProjectPreparer(targetPath);
        }
        paths = FileUtils.fileSearch(targetPath, MAVEN_BUILD_FILE_NAME);
        if (paths.length > 0) {
            return new MavenProjectPreparer(targetPath);
        }
        throw new IllegalArgumentException("Not a maven or gradle project");
    }

    public BuildType buildType();
    public Path getBuildScript();
    public void prepare();
}
