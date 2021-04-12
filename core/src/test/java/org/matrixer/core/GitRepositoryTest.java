package org.matrixer.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.junit.jupiter.api.*;

class GitRepositoryTest {

    final static String TEST_REPO_URL = "https://github.com/ikristola/matrixer-test";
    final static Path TMP_DIR = FileUtils.getSystemTempDirectory();
    final static Path TARGET_DIR = TMP_DIR.resolve("matrixer-test");

    @BeforeEach
    void setup() {
        System.out.println("Cleaning up!");
        FileUtils.removeDirectory(TARGET_DIR);
    }

    @Test
    void throwsExceptionIfTargetAlreadyExists() {
        createDirectory(TARGET_DIR);
        Path target = TARGET_DIR;
        assertThrows(Exception.class, () -> GitRepository.clone(TEST_REPO_URL, target));
    }

    @Test
    void downloadsRepositoryIfURLisValid() {
        Path target = TARGET_DIR;
        assertDoesNotThrow(() -> GitRepository.clone(TEST_REPO_URL, target));
        assertTrue(directoryContainsFile(target, ".git"));
    }

    @Test
    void throwsExceptionIfURLIsNotAGitRepository() {
        Path target = TARGET_DIR;
        assertThrows(GitAPIException.class,
                () -> GitRepository.clone(TEST_REPO_URL + "/does-not-exist", target));
    }

    @Test
    void canCloneLocalGitRepository() {
        Path target = TARGET_DIR;
        String localPath = projectDirectory().toString();
        assertDoesNotThrow(() -> GitRepository.clone(localPath, target));
        assertTrue(directoryContainsFile(target, ".git"));
    }

    @Test
    void throwsExceptionIfLocalPathIsNotAGitDirectory() {
        Path target = TARGET_DIR;
        String url = TMP_DIR.toString();
        assertThrows(GitAPIException.class, () -> GitRepository.clone(url, target));
    }

    @Test
    void canGetRootPath() throws GitAPIException {
        var repo = GitRepository.clone(TEST_REPO_URL, TARGET_DIR);
        assertEquals(TARGET_DIR, repo.rootDirectory());
    }

    @Test
    void canGetCleanStatus() throws NoWorkTreeException, GitAPIException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, TARGET_DIR);
        assertTrue(repo.isClean());
    }

    @Test
    void isNotCleanAfterAddingFile() throws NoWorkTreeException, GitAPIException, IOException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, TARGET_DIR);
        Files.createFile(repo.rootDirectory().resolve("new-file.txt"));
        assertFalse(repo.isClean());
    }

    @Test
    void canCleanUntrackedFiles() throws NoWorkTreeException, GitAPIException, IOException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, TARGET_DIR);
        Files.createFile(repo.rootDirectory().resolve("new-file.txt"));
        repo.clean();
        assertTrue(repo.isClean());
    }

    @Test
    void canRestoreUncommittedChanges() throws GitAPIException, IOException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, TARGET_DIR);
        Files.writeString(repo.rootDirectory().resolve("build.gradle"), "New content");

        repo.restoreUncommitted();

        assertFalse(repo.hasUncommittedChanges(), "Still uncommited changes");
    }

    @Test
    void canGetUntrackedStatus() throws NoWorkTreeException, GitAPIException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, TARGET_DIR);
        assertFalse(repo.hasUncommittedChanges());
    }

    @Test
    void hasUntrackedAfterModifyingFile() throws NoWorkTreeException, GitAPIException, IOException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, TARGET_DIR);
        Files.writeString(repo.rootDirectory().resolve("build.gradle"), "New content");
        assertTrue(repo.hasUncommittedChanges());
    }

    @Test
    void canRestoreRepository() throws IOException, NoWorkTreeException, GitAPIException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, TARGET_DIR);
        Files.writeString(repo.rootDirectory().resolve("build.gradle"), "New content");
        Files.createFile(repo.rootDirectory().resolve("new-file.txt"));

        repo.restore();

        assertTrue(repo.isClean(), "Still untracked files");
        // assertFalse(repo.hasUncommittedChanges(), "Still uncommitted
        // changes");
    }

    GitRepository cloneOrOpen(Path projectDir) throws GitAPIException, IOException {
        if (Files.exists(projectDir)) {
            var repo = GitRepository.open(projectDir);
            repo.restore();
        }
        return GitRepository.clone(TEST_REPO_URL, projectDir);
    }

    static Path projectDirectory() {
        return Path.of(System.getProperty("user.dir")).getParent();
    }

    private void createDirectory(Path dir) {
        FileUtils.createDirectory(dir);
    }

    private static boolean directoryContainsFile(Path dir, String fname) {
        return Files.exists(dir.resolve(fname));
    }

}
