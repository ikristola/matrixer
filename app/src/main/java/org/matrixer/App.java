package org.matrixer;

import java.io.File;
import java.lang.reflect.Field;

public class App {

    Properties properties;
    GitRepository repo;

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Throwable e) {
            System.err.println("Error:\n\t" + e.getMessage());
        }
    }

    private static void run(String[] args) throws Exception {
        if (args.length == 0 || containsHelpFlag(args)) {
            printUsage();
            return;
        }

        App app = new App(args);
        app.run();
    }

    App(String[] args) {
        properties = new Properties();
        properties.parse(args);
    }

    void run() throws Exception {
        if (!properties.isValid()) {
            throw new IllegalArgumentException(properties.reasonForFailure());
        }

        if (properties.isRemote()) {
            cloneRemoteRepository();
        } else {
            setupLocalRepository();
        }
        verifyOutputDirectoryExists();
        System.out.println("Project setup Successful!");
    }

    private void verifyOutputDirectoryExists() {
        if (!FileUtils.isExistingDirectory(properties.outputPath())) {
            if (!FileUtils.createDirectory(properties.outputPath())) {
                throw new IllegalArgumentException("Failed to create output directory");
            }
        }
    }

    private void cloneRemoteRepository() throws Exception {
        final var remoteURL = properties.remoteURL().toString();
        final var targetPath = properties.targetPath().toFile();
        System.out.println("Cloning from " + remoteURL + " to " + targetPath);
        repo = GitRepository.clone(remoteURL, targetPath);
        System.out.println("Cloning successful!");
    }

    private void setupLocalRepository() {
        if (!FileUtils.isExistingDirectory(properties.targetPath())) {
            throw new IllegalArgumentException(
                    "Target path does not exist: " + properties.targetPath());
        }
        System.out.println("Properties: "
                + "\n\tTarget path: " + properties.targetPath()
                + "\n\tOutput path: " + properties.outputPath()
                + "\n\tRemote: " + properties.remoteURL());


        if (!FileUtils.isExistingDirectory(properties.targetPath())) {
            System.err.println("Error:\n\t" + "Target path does not exist");
            return;
        }
        if (!FileUtils.isExistingDirectory(properties.outputPath())) {
            if (!FileUtils.createDirectory(properties.outputPath())) {
                System.err.println("Error:\n\t" + "Failed to create output directory");
                return;
            }
        }
    }

    static boolean containsHelpFlag(String[] args) {
        for (String arg : args) {
            if (arg.equals("--help") || arg.equals("-h")) {
                return true;
            }
        }
        return false;
    }

    static void printUsage() {
        System.err.println(
                "Usage: " + "\n\tmatrixer --target <path> [--output <path>] [--git <URL>]");
    }

}
