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
package org.matrixer.core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
     * @param url
     *            A remote url or local path to a repository
     * @param targetDirectory
     *            The directory in which the repository will be cloned to
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

    /**
     * Opens an existing git repository
     *
     * @param projectDir
     *            The path to the respository
     * @returns a GitRepository instance of the repository
     */
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

    /**
     * Returns the root directory of the repository
     */
    public Path rootDirectory() {
        return repo.getRepository().getWorkTree().toPath();
    }

    /**
     * Tests if the repository has any untracked files
     */
    public boolean isClean() throws NoWorkTreeException, GitAPIException {
        return repo.status().call().isClean();
    }

    /**
     * Tests if the repository has uncommitted changes
     */
    public boolean hasUncommittedChanges() throws NoWorkTreeException, GitAPIException {
        return repo.status().call().hasUncommittedChanges();
    }

    /**
     * Restores any uncomitted changes
     */
    public void restoreUncommitted() throws NoWorkTreeException, GitAPIException {
        repo.checkout().setAllPaths(true).call();
    }

    /**
     * Restores the working tree to the last commit on this branch.
     *
     * Removes any uncommitted changes an untracked files.
     */
    public void restore() throws NoWorkTreeException, GitAPIException {
        restoreUncommitted();
        clean();
    }

    /**
     * Clean the repository.
     *
     * Removes any untracked files.
     */
    public void clean() throws NoWorkTreeException, GitAPIException {
        var cleaned = repo.clean().setCleanDirectories(true).call();
        for (var file : cleaned) {
            System.out.println("Cleaned: " + file);
        }
    }

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
