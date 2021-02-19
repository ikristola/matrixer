package org.matrixer;

public class App {

    Properties properties;
    GitRepository repo;

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Throwable e) {
            System.err.println("Error: " + e.getMessage());
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
            System.err.println("Error:\n\t" + properties.reasonForFailure());
            return;
        }
        if (properties.isRemote()) {
            cloneRemoteRepository();
        }
    }

    private void cloneRemoteRepository() throws Exception {
        final var remoteURL = properties.remoteURL().toString();
        final var targetPath = properties.targetPath().toFile();
        System.out.println("Cloning from " + remoteURL + " to " + targetPath);
        repo = GitRepository.clone(remoteURL, targetPath);
        System.out.println("Cloning successful!");
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
