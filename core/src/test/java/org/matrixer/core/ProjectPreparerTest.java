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

class ProjectPreparerTest {

    Path targetDirectory = TestUtils.targetDirectory();
    URI testRepoURL = TestUtils.testRepoURL();
    String testPackage = TestUtils.targetRootPackage;

    @BeforeEach
    void setUp() throws GitAPIException, IOException {
        if (Files.exists(targetDirectory)) {
            var repo = GitRepository.open(targetDirectory);
            repo.restore();
            return;
        }
        GitRepository.clone(testRepoURL.toString(), targetDirectory);
    }

    @Test
    void testClonesRemoteRepositoryToTargetDirectory() throws GitAPIException, IOException {

        Path target = TestUtils.targetDirectory();
        FileUtils.removeDirectory(target);

        URI testRepoURL = TestUtils.testRepoURL();
        Properties properties = new Properties();
        properties.setTargetDir(target);
        properties.setTargetPackage(testPackage);
        properties.setRemoteURL(testRepoURL);

        ProjectPreparer preparer = new ProjectPreparer();
        Project project = preparer.prepare(properties);

        assertTrue(Files.exists(target.resolve(".git")));
        assertEquals(target, project.directory());
    }

    @Test
    void testCanUseLocalProject() throws IOException, GitAPIException {
        Path target = TestUtils.targetDirectory();
        Properties properties = new Properties();
        properties.setTargetPackage(testPackage);
        properties.setTargetDir(target);

        ProjectPreparer preparer = new ProjectPreparer();
        Project project = preparer.prepare(properties);

        assertEquals(target, project.directory());
    }

    @Test
    void throwsExceptionForUnsupportedProject() {
        Path emptyTarget = FileUtils.createTempDirectory();
        Properties properties = new Properties();
        properties.setTargetPackage(testPackage);
        properties.setTargetDir(emptyTarget);

        assertThrows(Exception.class, () -> new ProjectPreparer().prepare(properties));
    }

    @Nested
    class Gradle {
        @Test
        void prepareInjectsGradleBuildScript() throws GitAPIException, IOException {
            Path target = TestUtils.targetDirectory();
            Properties properties = new Properties();
            properties.setShouldInstrument(true);
            properties.setTargetPackage(testPackage);
            properties.setTargetDir(target);
            ProjectPreparer preparer = new ProjectPreparer();

            Project project = preparer.prepare(properties);
            String agentString = preparer.agentString(project);
            String expected = "jvmArgs \"" + agentString + "\"";
            assertFileContainsString(project.buildScript(), expected);
        }
    }

    @Nested
    class Maven {
        @BeforeEach
        void setupMaven() {
            TestUtils.removeGradleFiles(targetDirectory);
        }

        @Test
        void prepareInjectsMavenBuildScript() throws GitAPIException, IOException {
            Path target = TestUtils.targetDirectory();
            Properties properties = new Properties();
            properties.setShouldInstrument(true);
            properties.setTargetPackage(testPackage);
            properties.setTargetDir(target);
            ProjectPreparer preparer = new ProjectPreparer();

            Project project = preparer.prepare(properties);
            String agentString = preparer.agentString(project);
            String agentArg = "<argLine>" + agentString + "</argLine>";
            String manifest = "<useManifestOnlyJar>false</useManifestOnlyJar>";
            assertFileContainsString(project.buildScript(), agentArg);
            assertFileContainsString(project.buildScript(), manifest);
        }
    }

    void assertFileContainsString(Path file, String string) {
        try {
            var result = FileUtils.searchInFile(string, file);
            if (!result.isPresent()) {
                throw new AssertionError(file + " did not contain:\n" + string);
            }
            Integer index = result.get();
            assertTrue(index != -1, "Did not find\n'" + string + "'\nin\n" + file + "\n");
        } catch (IOException e) {
            throw new AssertionError("assertFileContainsString: " + e.getMessage());
        }
    }
}
