package org.matrixer.core;

import java.io.IOException;
import java.nio.file.Path;

import java.nio.file.Files;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * Creates and manages git repositories
 */
public class GitRepository {

    Git repo;

    private GitRepository(Git repo, Path root) {
        this.repo = repo;
    }

    /**
     * Clones a git repository located at url to target
     *
     * @param url             A remote url or local path to a repository
     * @param targetDirectory The directory in which the repository will be
     *                        cloned to
     * @returns a GitRepository instance of the repository
     */
    public static GitRepository clone(String url, Path targetDirectory) throws GitAPIException {
        if (Files.exists(targetDirectory)) {
            throw new RuntimeException("Directory already exists:" + targetDirectory);
        }
        System.out.println("Cloning from " + url + " to " + targetDirectory);
        Git result = Git.cloneRepository()
                .setURI(url)
                .setDirectory(targetDirectory.toFile())
                .setProgressMonitor(new SimpleProgressMonitor())
                .call();
        return new GitRepository(result, targetDirectory);
    }

    public static GitRepository open(Path projectDir) throws IOException {
        Path repoDir = projectDir.resolve(".git");
        if (!Files.exists(repoDir)) {
            throw new RuntimeException("Not a git repository");
        }
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(repoDir.toFile())
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();
        return new GitRepository(new Git(repository), projectDir);
    }

    public Path rootDirectory() {
        return repo.getRepository().getWorkTree().toPath();
    }

    public boolean isClean() throws NoWorkTreeException, GitAPIException {
        return repo.status().call().isClean();
    }

    public boolean hasUncommittedChanges() throws NoWorkTreeException, GitAPIException {
        return repo.status().call().hasUncommittedChanges();
    }

    public void restoreUncommitted() throws NoWorkTreeException, GitAPIException {
        repo.checkout().setAllPaths(true).call();
    }

    public void restore() throws NoWorkTreeException, GitAPIException {
        restoreUncommitted();
        clean();
    }

    public void clean() throws NoWorkTreeException, GitAPIException {
        var cleaned = repo.clean().setCleanDirectories(true).call();
        for (var file : cleaned) {
            System.out.println("Cleaned: " + file);
        }
    }

    // Clean: no untracked files
    // Uncomitted changes: files modified

    /**
     * Prints progress messages to standard out
     */
    private static class SimpleProgressMonitor implements ProgressMonitor {

        @Override
        public void start(int totalTasks) {
            System.out.println("Starting work on " + totalTasks + " tasks");
        }

        @Override
        public void beginTask(String title, int totalWork) {
            System.out.print(title + ": " + totalWork);
            if (title.startsWith("remote:")) {
                System.out.println();
            } else {
                System.out.print(" ... ");
            }
        }

        @Override
        public void update(int completed) {
        }

        @Override
        public void endTask() {
            System.out.println("Done");
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}
