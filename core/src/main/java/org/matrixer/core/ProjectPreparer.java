package org.matrixer.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.matrixer.core.util.FileUtils;
import org.matrixer.core.util.GitRepository;
import org.matrixer.core.runtime.AgentOptions;

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
        String agentString = agentString(project);
        project.injectBuildScript(agentString);

        if (!properties.analyzeOnly() && !properties.instrumentOnly()) {
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
