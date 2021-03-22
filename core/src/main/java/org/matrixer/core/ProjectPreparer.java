package org.matrixer.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;

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
        String agentString = agentString(properties);
        project.injectBuildScript(agentString);
        Files.createDirectories(project.outputDirectory());
        return project;
    }

    String agentString(Properties properties) {
        String outputDir = project.outputDirectory().toString();
        String agentJarPath = pathToAgent().toString();
        String targetPackage = project.targetPackage();
        String testPackage = project.testPackage();

        return String.format("-javaagent:%s=%s:%s:%s",
                agentJarPath, outputDir, targetPackage, testPackage);
    }

    Path pathToAgent() {
        String cwd = System.getProperty("user.dir");
        return Path.of(cwd, "../agent/build/libs/agentJar.jar").normalize();
    }
}
