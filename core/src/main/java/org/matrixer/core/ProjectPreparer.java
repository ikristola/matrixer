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

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.matrixer.core.runtime.AgentOptions;
import org.matrixer.core.util.FileUtils;
import org.matrixer.core.util.GitRepository;

/**
 * Prepares a target project for further processing
 */
public class ProjectPreparer {

    Project project;

    public Project prepare(Properties properties) throws GitAPIException, IOException {
        if (properties.isRemote()) {
            GitRepository.clone(
                    properties.remoteURL().toString(),
                    properties.targetDir());
        }
        this.project = ProjectFactory.from(properties);

        if (properties.shouldInstrument()) {
            String agentString = agentString(project);
            project.injectBuildScript(agentString);
        }
        if (properties.shouldRun()) {
            FileUtils.replaceExisting(project.outputDirectory());
        }
        return project;
    }

    String agentString(Project project) {
        Path destfile = project.resultsFile();
        AgentOptions options = new AgentOptions();
        options.setDestFilename(destfile.toString());
        options.setDepthLimit(project.properties.getDepthLimit());
        options.setTargetPackage(project.targetPackage());
        options.setTestPackage(project.testPackage());
        if (project.properties.getDebug()) {
            options.setDebug(true);
        }

        return options.getJVMArgument(pathToAgent());
    }

    Path pathToAgent() {
        String cwd = System.getProperty("user.dir");
        return Path.of(cwd, "../agent/build/libs/agentJar.jar").normalize();
    }
}
