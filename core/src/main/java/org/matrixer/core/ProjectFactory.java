package org.matrixer.core;

import java.nio.file.Files;
import java.nio.file.Path;

class ProjectFactory {
    static Project scan(Path targetDir) {
        Properties prop = new Properties();
        prop.setTargetDir(targetDir);
        return from(prop);
    }

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
