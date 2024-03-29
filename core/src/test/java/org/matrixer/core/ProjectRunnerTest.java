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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.*;
import org.matrixer.core.testsupport.TestUtils;
import org.matrixer.core.util.FileUtils;
import org.matrixer.core.util.GitRepository;

public class ProjectRunnerTest {

    Path targetDirectory = TestUtils.targetDirectory();
    URI testRepoURL = TestUtils.testRepoURL();
    String targetPackage = TestUtils.targetRootPackage;

    @BeforeEach
    void setUp() throws GitAPIException, IOException {
        if (Files.exists(targetDirectory)) {
            var repo = GitRepository.open(targetDirectory);
            repo.restore();
            return;
        }
        GitRepository.clone(testRepoURL.toString(), targetDirectory);
    }

    @Nested
    class Gradle {
        @Test
        void canRunProjectTests() throws GitAPIException, IOException {
            Path target = TestUtils.targetDirectory();
            Properties properties = new Properties();
            properties.setShouldRun(true);
            properties.setTargetPackage(targetPackage);
            properties.setTargetDir(target);
            ProjectPreparer preparer = new ProjectPreparer();

            Project project = preparer.prepare(properties);
            ProjectRunner runner = new ProjectRunner();
            int status = runner.runTests(project);
            assertEquals(0, status);
            assertTrue(Files.exists(project.logFile()));
        }

        @Test
        void redirectsProcessStreamsToLogFile() throws GitAPIException, IOException {
            Path target = TestUtils.targetDirectory();
            Properties properties = new Properties();
            properties.setShouldRun(true);
            properties.setTargetPackage(targetPackage);
            properties.setTargetDir(target);
            ProjectPreparer preparer = new ProjectPreparer();

            Project project = preparer.prepare(properties);
            ProjectRunner runner = new ProjectRunner();
            runner.runTests(project);
            assertTrue(Files.exists(project.logFile()));
        }

        @Test
        void throwsExceptionIfOutputDirectoryDoesNotExist() throws GitAPIException, IOException {
            Path target = TestUtils.targetDirectory();
            Properties properties = new Properties();
            properties.setShouldRun(true);
            properties.setTargetPackage(targetPackage);
            properties.setTargetDir(target);
            ProjectPreparer preparer = new ProjectPreparer();
            Project project = preparer.prepare(properties);
            FileUtils.removeDirectory(project.outputDirectory());

            ProjectRunner runner = new ProjectRunner();
            assertThrows(Exception.class, () -> runner.runTests(project));
        }

        @Test
        void returnsNonZeroIfProcessExitsWithError() throws GitAPIException, IOException {
            Path target = TestUtils.targetDirectory();
            Properties properties = new Properties();
            properties.setShouldRun(true);
            properties.setTargetPackage(targetPackage);
            properties.setTargetDir(target);
            ProjectPreparer preparer = new ProjectPreparer();
            Project project = preparer.prepare(properties);
            Files.deleteIfExists(project.buildScript());

            ProjectRunner runner = new ProjectRunner();
            int status = runner.runTests(project);
            assertNotEquals(0, status);
        }
    }

    @Nested
    class Maven {

        @BeforeEach
        void setupMaven() {
            TestUtils.removeGradleFiles(targetDirectory);
        }

        @Test
        void canRunProjectTests() throws GitAPIException, IOException {
            Path target = TestUtils.targetDirectory();
            Properties properties = new Properties();
            properties.setShouldRun(true);
            properties.setTargetPackage(targetPackage);
            properties.setTargetDir(target);
            ProjectPreparer preparer = new ProjectPreparer();

            Project project = preparer.prepare(properties);
            ProjectRunner runner = new ProjectRunner();
            int status = runner.runTests(project);
            assertEquals(0, status);
            assertTrue(Files.exists(project.logFile()));
        }

        @Test
        void redirectsProcessStreamsToLogFile() throws GitAPIException, IOException {
            Path target = TestUtils.targetDirectory();
            Properties properties = new Properties();
            properties.setShouldRun(true);
            properties.setTargetPackage(targetPackage);
            properties.setTargetDir(target);
            ProjectPreparer preparer = new ProjectPreparer();

            Project project = preparer.prepare(properties);
            ProjectRunner runner = new ProjectRunner();
            runner.runTests(project);
            assertTrue(Files.exists(project.logFile()));
        }

        @Test
        void throwsExceptionIfOutputDirectoryDoesNotExist() throws GitAPIException, IOException {
            Path target = TestUtils.targetDirectory();
            Properties properties = new Properties();
            properties.setShouldRun(true);
            properties.setTargetPackage(targetPackage);
            properties.setTargetDir(target);
            ProjectPreparer preparer = new ProjectPreparer();
            Project project = preparer.prepare(properties);
            FileUtils.removeDirectory(project.outputDirectory());

            ProjectRunner runner = new ProjectRunner();
            assertThrows(Exception.class, () -> runner.runTests(project));
        }

        @Test
        void returnsNonZeroIfProcessExitsWithError() throws GitAPIException, IOException {
            Path target = TestUtils.targetDirectory();
            Properties properties = new Properties();
            properties.setShouldRun(true);
            properties.setTargetPackage(targetPackage);
            properties.setTargetDir(target);
            ProjectPreparer preparer = new ProjectPreparer();
            Project project = preparer.prepare(properties);
            Files.deleteIfExists(project.buildScript());

            ProjectRunner runner = new ProjectRunner();
            int status = runner.runTests(project);
            assertNotEquals(0, status);
        }
    }
}
