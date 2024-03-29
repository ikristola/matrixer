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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.*;
import org.matrixer.core.testsupport.TestUtils;
import org.matrixer.core.util.FileUtils;
import org.matrixer.core.util.GitRepository;

class ProjectTest {

    final static String TEST_REPO_URL = "https://github.com/ikristola/matrixer-test";
    static Path TARGET_DIR = FileUtils.getSystemTempDirectory().resolve("matrixer-test");
    static String targetPackage = TestUtils.targetRootPackage;

    static GitRepository git;

    @BeforeEach
    void setUp() throws GitAPIException, IOException {
        if (Files.exists(TARGET_DIR)) {
            var repo = GitRepository.open(TARGET_DIR);
            repo.restore();
            return;
        }
        GitRepository.clone(TEST_REPO_URL, TARGET_DIR);
    }

    @Nested
    class GradleProject {
        @Test
        void testFindsGradleBuildScript() throws IOException {
            Project project = ProjectFactory.scan(TARGET_DIR);
            assertEquals(project.buildScript(), TARGET_DIR.resolve("build.gradle"));
        }

        @Test
        void testSetsCorrectOutputPath() throws IOException {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path outputDir = Path.of("build", "matrixer-cov", "depth-0");
            assertEquals(TARGET_DIR.resolve(outputDir), project.outputDirectory());
        }

        @Test
        void canInitializeFromProperties() {
            Properties prop = new Properties();
            URI remoteURL = TestUtils.asURI(TEST_REPO_URL);
            prop.setTargetDir(TARGET_DIR);
            prop.setTargetPackage(targetPackage);
            prop.setRemoteURL(remoteURL);

            Project project = ProjectFactory.from(prop);

            Path buildScript = TARGET_DIR.resolve("build.gradle");
            Path outputDir = TARGET_DIR.resolve("build/matrixer-cov");

            assertEquals(TARGET_DIR, project.directory());
            assertEquals(outputDir.resolve("depth-0"), project.outputDirectory());
            assertEquals(buildScript, project.buildScript());
            assertEquals(remoteURL, project.remoteURL());
        }

        @Test
        void testReturnsSpecifiedOutputPathIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setOutputDir(outputDir);
            prop.setTargetPackage(targetPackage);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            assertEquals(TARGET_DIR, project.directory());
            assertEquals(outputDir.resolve("depth-0"), project.outputDirectory());
        }

        @Test
        void logFileIsInOutputDirectoryWithDefaults() {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path logFile = Path.of("matrixer-log.txt");
            assertEquals(project.outputDirectory().resolve(logFile), project.logFile());
        }

        @Test
        void resultsFileIsInOutputDirectoryWithDefaults() {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path resultsFile = Path.of("matrixer-results.txt");

            Path expected = project.outputDirectory().resolve(resultsFile);
            assertEquals(expected, project.resultsFile());
        }

        @Test
        void logFileIsInOutputDirectoryIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setOutputDir(outputDir);
            prop.setTargetPackage(targetPackage);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            Path logFile = Path.of("matrixer-log.txt");
            Path expected = project.outputDirectory().resolve(logFile);
            assertEquals(expected, project.logFile());
        }

        @Test
        void resultsFileIsInOutputDirectoryIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setOutputDir(outputDir);
            prop.setTargetPackage(targetPackage);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            Path resultsFile = Path.of("matrixer-results.txt");
            Path expected = project.outputDirectory().resolve(resultsFile);
            assertEquals(expected, project.resultsFile());
        }
    } // End Gradle Project

    @Nested
    class MavenProject {

        @BeforeEach
        void setupMaven() {
            TestUtils.removeGradleFiles(TARGET_DIR);
        }

        @Test
        void testFindsGradleBuildScript() throws IOException {
            Project project = ProjectFactory.scan(TARGET_DIR);
            assertEquals(project.buildScript(), TARGET_DIR.resolve("pom.xml"));
        }

        @Test
        void testSetsCorrectOutputPath() throws IOException {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path outputDir = Path.of("target", "matrixer-cov", "depth-0");
            assertEquals(TARGET_DIR.resolve(outputDir), project.outputDirectory());
        }

        @Test
        void canInitializeFromProperties() {
            Properties prop = new Properties();
            URI remoteURL = TestUtils.asURI(TEST_REPO_URL);
            prop.setTargetDir(TARGET_DIR);
            prop.setTargetPackage(targetPackage);
            prop.setRemoteURL(remoteURL);

            Project project = ProjectFactory.from(prop);

            Path buildScript = TARGET_DIR.resolve("pom.xml");
            Path outputDir = TARGET_DIR.resolve("target/matrixer-cov");

            assertEquals(TARGET_DIR, project.directory());
            assertEquals(outputDir.resolve("depth-0"), project.outputDirectory());
            assertEquals(buildScript, project.buildScript());
            assertEquals(remoteURL, project.remoteURL());
        }

        @Test
        void testReturnsSpecifiedOutputPathIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setTargetPackage(targetPackage);
            prop.setOutputDir(outputDir);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            assertEquals(TARGET_DIR, project.directory());
            assertEquals(outputDir.resolve("depth-0"), project.outputDirectory());
        }

        @Test
        void logFileIsInOutputDirectoryWithDefaults() {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path logFile = Path.of("matrixer-log.txt");

            Path expected = project.outputDirectory().resolve(logFile);
            assertEquals(expected, project.logFile());
        }

        @Test
        void resultsFileIsInOutputDirectoryWithDefaults() {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path resultsFile = Path.of("matrixer-results.txt");

            Path expected = project.outputDirectory().resolve(resultsFile);
            assertEquals(expected, project.resultsFile());
        }

        @Test
        void logFileIsInOutputDirectoryIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setTargetPackage(targetPackage);
            prop.setOutputDir(outputDir);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            Path logFile = Path.of("matrixer-log.txt");
            Path expected = project.outputDirectory().resolve(logFile);
            assertEquals(expected, project.logFile());
        }

        @Test
        void resultsFileIsInOutputDirectoryIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setTargetPackage(targetPackage);
            prop.setOutputDir(outputDir);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            Path resultsFile = Path.of("matrixer-results.txt");
            Path expected = project.outputDirectory().resolve(resultsFile);
            assertEquals(expected, project.resultsFile());
        }
    } // End Maven Project

}
