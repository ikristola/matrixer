package org.matrixer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Executes the test suite of target Gradle-based java project
 */
public class ProjectRunner {

    private Path projectPath;
    private Path logFileDir;
    private String logFileName;
    private String task;
    private String buildSystem;

    public static class Builder {
        private Path projectPath;
        private Path logFileDir = Path.of(System.getProperty("java.io.tmpdir"));
        private String logFileName = "log.txt";
        private String task = "help";
        private String buildSystem = "maven";

        Builder() {
        }

        Builder projectPath(Path projectPath) {
            return projectPath(projectPath.toString());
        }

        Builder projectPath(String projectPath) {
            this.projectPath = Path.of(projectPath);
            return this;
        }

        Builder logFileName(String logFile) {
            this.logFileName = logFile;
            return this;
        }

        Builder logFileDir(Path logFileDir) {
            this.logFileDir = logFileDir;
            return this;
        }

        Builder logFileDir(String logFileDir) {
            return logFileDir(Path.of(logFileDir));
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
            if (!Files.exists(projectPath)) {
                throw new RuntimeException("Project path does not exits: " + projectPath);
            }
            if (!Files.exists(logFileDir)) {
                throw new RuntimeException("Log file path does not exits: " + logFileDir);
            }
            ProjectRunner projectRunner = new ProjectRunner();
            projectRunner.projectPath = this.projectPath;
            projectRunner.logFileName = this.logFileName;
            projectRunner.logFileDir = this.logFileDir;
            projectRunner.task = this.task;
            projectRunner.buildSystem = this.buildSystem;
            return projectRunner;
        }
    }

    private ProjectRunner() {
    }

    /**
     * Runs the project.
     *
     * The output streams will be redirected to the log file.
     */
    public int run() {
        switch (buildSystem) {
        case "gradle":
            return runGradle();
        case "maven":
            return runMaven();
        default:
            throw new RuntimeException("No build system set");
        }
    }

    private int runGradle() {
        ProcessBuilder builder = buildGradleProcess();
        return runProcess(builder);
    }

    private ProcessBuilder buildGradleProcess() {
        ProcessBuilder builder = new ProcessBuilder("gradle", task, "-p", projectPath.toString());
        redirectStreams(builder);
        return builder;
    }

    private int runMaven() {
        ProcessBuilder builder = buildMavenProcess();
        return runProcess(builder);
    }

    private ProcessBuilder buildMavenProcess() {
        if (!Files.exists(projectPath)) {
            throw new RuntimeException("WHAAAAT!");
        }
        ProcessBuilder builder = new ProcessBuilder()
            .command("mvn", task)
            .directory(projectPath.toFile());
        redirectStreams(builder);
        return builder;
    }

    private void redirectStreams(ProcessBuilder builder) {
        builder.redirectErrorStream(true);
        var file = getLogFile().toFile();
        System.out.println("Redirecting to" + file);
        builder.redirectOutput(file);
    }

    private int runProcess(ProcessBuilder builder) {
        try {
            Process process = builder.start();
            System.out.println("Waiting for project to finish...");
            return process.waitFor();
        } catch (IOException e) {
            throw new RuntimeException("runProcess: " + e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Running project failed: " + e.getMessage());
        }
    }

    /**
     * Returns the path to the log file
     */
    public Path getLogFile() {
        return logFileDir.resolve(logFileName);
    }

    /**
     * Returns the path to the parent directory of the log file
     */
    public Path getLogFileDir() {
        return logFileDir;
    }

    /**
     * Returns the name of the log file
     */
    public String getLogFileName() {
        return logFileName;
    }

    /**
     * Returns the path to the project directory
     */
    public Path getProjectDir() {
        return projectPath;
    }

    /**
     * Returns the name of the build system task to run
     */
    public String getTask() {
        return task;
    }

    /**
     * Returns the build system used by this project runner
     */
    public String getBuildSystem() {
        return buildSystem;
    }
}
