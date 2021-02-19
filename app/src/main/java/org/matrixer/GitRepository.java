package org.matrixer;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;

public class GitRepository {

    Git repo;

    private GitRepository(Git repo) {
        this.repo = repo;
    }

    public static GitRepository clone(String url, File target) throws GitAPIException {
        if (target.exists()) {
            throw new RuntimeException("Directory already exists:" + target);
        }
        System.out.println("Cloning from " + url + " to " + target);
        Git result = Git.cloneRepository()
                .setURI(url)
                .setDirectory(target)
                .setProgressMonitor(new SimpleProgressMonitor())
                .call();
        return new GitRepository(result);
    }


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
