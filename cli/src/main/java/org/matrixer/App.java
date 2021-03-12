package org.matrixer;

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
        prepareProject();
        System.out.println("Project setup Successful!");
        runProject();
    }

    private void prepareProject() {
        System.out.println("Preparing target project");
        ProjectPreparer projectPreparer = ProjectPreparer.scan(properties);
        projectPreparer.prepare();
    }

    private void runProject() {
        System.out.println("Running target project tests");
            ProjectRunner projectRunner = new ProjectRunner.Builder()
                    .projectPath(properties.targetDir())
                    .logFileDir(properties.outputDir())
                    .logFileName("matrixer-target-runlog.txt")
                    .task("test")
                    .buildSystem("gradle")
                    .build();
            int status = projectRunner.run();
            if (status != 0) {
                System.out.println("Target project tests exited with error: " + status);
                return;
            }
            System.out.println("Target project tests was run successfully!");
    }

    private void verifyOutputDirectoryExists() {
        if (!FileUtils.isExistingDirectory(properties.outputDir())) {
            if (!FileUtils.createDirectory(properties.outputDir())) {
                throw new IllegalArgumentException("Failed to create output directory");
            }
        }
    }

    private void cloneRemoteRepository() throws Exception {
        final var remoteURL = properties.remoteURL().toString();
        final var targetPath = properties.targetDir().toFile();
        System.out.println("Cloning from " + remoteURL + " to " + targetPath);
        repo = GitRepository.clone(remoteURL, targetPath);
        System.out.println("Cloning successful!");
    }

    private void setupLocalRepository() {
        if (!FileUtils.isExistingDirectory(properties.targetDir())) {
            throw new IllegalArgumentException(
                    "Target path does not exist: " + properties.targetDir());
        }
        System.out.println("Properties: "
                + "\n\tTarget path: " + properties.targetDir()
                + "\n\tOutput path: " + properties.outputDir()
                + "\n\tRemote: " + properties.remoteURL());
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
