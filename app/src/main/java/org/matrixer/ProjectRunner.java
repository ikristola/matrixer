package org.matrixer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Executes the test suite of target Gradle-based java project
 */
public class ProjectRunner {

    private String projectPath;
    private String logFilePath;
    private String logFileName;
    private String task;
    private String buildSystem;

    public static class Builder {
        private String projectPath;
        private String logFilePath = System.getProperty("java.io.tmpdir");
        private String logFileName = "log.txt";
        private String task = "help";
        private String buildSystem = "maven";

        Builder() {}

        Builder projectPath(Path projectPath) {
            return projectPath(projectPath.toString());
        }

        Builder projectPath(String projectPath) {
            this.projectPath = projectPath;
            return this;
        }

        Builder logFileName(String logFile) {
            this.logFileName = logFile;
            return this;
        }

        Builder logFilePath(Path logFilePath) {
            return logFilePath(logFilePath.toString());
        }

        Builder logFilePath(String logFilePath) {
            this.logFilePath = logFilePath;
            return this;
        }

        Builder task(String task) {
            this.task = task;
            return this;
        }

        Builder buildSystem(String buildSystem) {
            this.buildSystem = buildSystem;
            return this;
        }

        ProjectRunner build() {
            ProjectRunner projectRunner = new ProjectRunner();
            projectRunner.projectPath = this.projectPath;
            projectRunner.logFilePath = this.logFilePath;
            projectRunner.logFileName = this.logFileName;
            projectRunner.task = this.task;
            projectRunner.buildSystem = this.buildSystem;
            return projectRunner;
        }
    }

    private ProjectRunner() {}

    int run() throws IOException {
        ProcessBuilder processBuilder;

        if (buildSystem.equals("gradle")) {
            processBuilder = new ProcessBuilder(
                    "gradle", task, "-p", projectPath);
        } else {
            throw new RuntimeException("No build system set");
        }

        File logFile = new File(logFilePath + File.separator + logFileName);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(logFile);

        Process process = processBuilder.start();
        System.out.println("Waiting for project to finish...");
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(
                    "Could not wait for project to finish: " + e.getMessage());
        }
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getTask() {
        return task;
    }

    public String getBuildSystem() {
        return buildSystem;
    }
}
