package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.junit.jupiter.api.*;

class GitRepositoryTest {

    final static String TEST_REPO_URL = "https://github.com/ikristola/matrixer-test";
    final static String TMP_DIR = System.getProperty("java.io.tmpdir");
    final static Path TARGET_DIR = Path.of(TMP_DIR, File.separator, "matrixer-test");

    @BeforeEach
    void setup() {
        System.out.println("Cleaning up!");
        removeDirectory(TARGET_DIR);
    }

    @Test
    void throwsExceptionIfTargetAlreadyExists() {
        createDirectory(TARGET_DIR);
        File target = temporaryTargetFile();
        assertThrows(Exception.class, () -> GitRepository.clone(TEST_REPO_URL, target));
    }

    @Test
    void downloadsRepositoryIfURLisValid() {
        File target = temporaryTargetFile();
        assertDoesNotThrow(() -> GitRepository.clone(TEST_REPO_URL, target));
        assertTrue(directoryContainsFile(target, ".git"));
    }

    @Test
    void throwsExceptionIfURLIsNotAGitRepository() {
        File target = temporaryTargetFile();
        assertThrows(GitAPIException.class,
                () -> GitRepository.clone(TEST_REPO_URL + "/does-not-exist", target));
    }

    @Test
    void canCloneLocalGitRepository() {
        File target = temporaryTargetFile();
        String localPath = projectDirectory().toString();
        assertDoesNotThrow(() -> GitRepository.clone(localPath, target));
        assertTrue(directoryContainsFile(target, ".git"));
    }

    @Test
    void throwsExceptionIfLocalPathIsNotAGitDirectory() {
        File target = temporaryTargetFile();
        assertThrows(GitAPIException.class, () -> GitRepository.clone(TMP_DIR, target));
    }

    @Test
    void canGetRootPath() throws GitAPIException {
        var repo = GitRepository.clone(TEST_REPO_URL, temporaryTargetFile());
        assertEquals(temporaryTargetFile().toPath(), repo.rootDirectory());
    }

    @Test
    void canGetCleanStatus() throws NoWorkTreeException, GitAPIException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, temporaryTargetFile());
        assertTrue(repo.isClean());
    }

    @Test
    void isNotCleanAfterAddingFile() throws NoWorkTreeException, GitAPIException, IOException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, temporaryTargetFile());
        Files.createFile(repo.rootDirectory().resolve("new-file.txt"));
        assertFalse(repo.isClean());
    }

    @Test
    void canCleanUntrackedFiles() throws NoWorkTreeException, GitAPIException, IOException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, temporaryTargetFile());
        Files.createFile(repo.rootDirectory().resolve("new-file.txt"));
        repo.clean();
        assertTrue(repo.isClean());
    }

    @Test
    void canRestoreUncommittedChanges() throws GitAPIException, IOException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, temporaryTargetFile());
        Files.writeString(repo.rootDirectory().resolve("build.gradle"), "New content");

        repo.restoreUncommitted();

        assertFalse(repo.hasUncommittedChanges(), "Still uncommited changes");
    }

    @Test
    void canGetUntrackedStatus() throws NoWorkTreeException, GitAPIException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, temporaryTargetFile());
        assertFalse(repo.hasUncommittedChanges());
    }

    @Test
    void hasUntrackedAfterModifyingFile() throws NoWorkTreeException, GitAPIException, IOException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, temporaryTargetFile());
        Files.writeString(repo.rootDirectory().resolve("build.gradle"), "New content");
        assertTrue(repo.hasUncommittedChanges());
    }

    @Test
    void canRestoreRepository() throws IOException, NoWorkTreeException, GitAPIException {
        GitRepository repo = GitRepository.clone(TEST_REPO_URL, temporaryTargetFile());
        Files.writeString(repo.rootDirectory().resolve("build.gradle"), "New content");
        Files.createFile(repo.rootDirectory().resolve("new-file.txt"));

        repo.restore();

        assertTrue(repo.isClean(), "Still untracked files");
        // assertFalse(repo.hasUncommittedChanges(), "Still uncommitted changes");
    }

    GitRepository cloneOrOpen(Path projectDir) throws GitAPIException, IOException {
        if (Files.exists(projectDir)) {
            var repo = GitRepository.open(projectDir);
            repo.restore();
        }
        return GitRepository.clone(TEST_REPO_URL, projectDir.toFile());
    }

    static File projectDirectory() {
        return new File(System.getProperty("user.dir")).getParentFile();
    }

    static File temporaryTargetFile() {
        return TARGET_DIR.toFile();
    }

    private void createDirectory(Path dir) {
        FileUtils.createDirectory(dir);
    }

    private static boolean directoryContainsFile(File dir, String fname) {
        return new File(dir, fname).exists();
    }

    private static void removeDirectory(Path dir) {
        try {
            if (dir.toFile().exists()) {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            throw new RuntimeException("Clean up: Failed to remove dir: " + e.getMessage());
        }
    }
}
