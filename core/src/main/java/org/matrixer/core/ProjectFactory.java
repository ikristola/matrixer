package org.matrixer.core;

import java.nio.file.Files;
import java.nio.file.Path;

class ProjectFactory {

    /**
     * Creates a Project by scanning targetDir for Maven and Gradle build
     * scripts and returns a suitible Project.
     *
     * @param targetDir
     *            the directory to scan
     *
     * @returns the Project instance
     *
     * @throws IllegalArgumentException
     *             if the project does not contain any valid build scripts
     */
    static Project scan(Path targetDir) {
        Properties prop = new Properties();
        prop.setTargetDir(targetDir);
        return from(prop);
    }

    /**
     * Creates a Project as specified by the Properties.
     *
     * This method scans the project directory for Maven and Gradle build
     * scripts and returns a suitible Project.
     *
     * @returns the Project instance
     *
     * @throws IllegalArgumentException
     *             if the project does not contain any valid build scripts
     */
    static Project from(Properties prop) {
        if (!Files.exists(prop.targetDir())) {
            throw new IllegalArgumentException("");
        }
        var paths = FileUtils.fileSearch(prop.targetDir(), GradleProject.scriptName);
        if (paths.length > 0) {
            return new GradleProject(prop);
        }
        paths = FileUtils.fileSearch(prop.targetDir(), MavenProject.scriptName);
        if (paths.length > 0) {
            return new MavenProject(prop);
        }
        throw new IllegalArgumentException("Not a maven or gradle project");
    }
}
