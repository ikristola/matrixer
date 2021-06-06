/**
 * Copyright 2021 Patrik Bogren, Isak Kristola
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.matrixer.core;

import java.nio.file.Files;
import java.nio.file.Path;

import org.matrixer.core.util.FileUtils;

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
            throw new IllegalArgumentException("Directory does not exist: " + prop.targetDir());
        }
        // Not all projects have a build script in the project root directory.
        // But it seems reasonable to assume that one exists in at most depth 2
        // of the project directory. Any deeper structure than that is probably to
        // difficult to examine automatically.
        final int maxDepth = 2;
        for (int i = 0; i < maxDepth; i++) {
            final int depth = i + 1;
            if (findGradleScript(prop.targetDir(), depth)) {
                return new GradleProject(prop);
            }
            if (findMavenScript(prop.targetDir(), depth)) {
                return new MavenProject(prop);
            }
        }
        throw new IllegalArgumentException("Not a maven or gradle project");
    }

    static boolean findGradleScript(Path base, int depth) {
        var paths = FileUtils.findFiles(base, GradleProject.scriptName, depth);
        return paths.length > 0;
    }

    static boolean findMavenScript(Path base, int depth) {
        var paths = FileUtils.findFiles(base, MavenProject.scriptName, depth);
        return paths.length > 0;
    }
}
