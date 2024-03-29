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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Executes the test suite of target Gradle-based java project
 */
public class ProjectRunner {

    /**
     * Runs the project.
     *
     * The output streams will be redirected to the log file. Note: if
     * running a cleaning task, such as 'gradle clean', it will remove the
     * output directory
     */
    public int runTests(Project project) {
        if (!Files.isDirectory(project.outputDirectory())) {
            Path dir = project.outputDirectory();
            throw new RuntimeException("ProjectRunner: output directory does not exist: " + dir);
        }
        List<String> cmd = project.getTestCommand();
        ProcessBuilder builder = new ProcessBuilder()
                .directory(project.directory().toFile())
                .command(cmd);
        redirectStreams(builder, project.logFile());
        return runProcess(builder);
    }

    private void redirectStreams(ProcessBuilder builder, Path logFile) {
        builder.redirectErrorStream(true);
        System.out.println("Logile: " + logFile);
        builder.redirectOutput(logFile.toFile());
    }

    private int runProcess(ProcessBuilder builder) {
        try {
            Process process = builder.start();
            // Make sure child process terminates when JVM terminates
            Runtime.getRuntime().addShutdownHook(new Thread(process::destroyForcibly));
            System.out.println("Waiting for project to finish...");
            return process.waitFor();
        } catch (IOException e) {
            throw new RuntimeException("ProjectRunner: I/O error: " + e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Running project failed: " + e.getMessage());
        }
    }
}
