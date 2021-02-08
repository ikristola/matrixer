package org.matrixer;

public class App {

    Properties properties;

    public static void main(String[] args) {
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

    void run() {
        if (!properties.isValid()) {
            System.err.println("Error:\n\t" + properties.reasonForFailure());
            return;
        }
        System.out.println("Properties: "
                + "\n\tTarget path: " + properties.targetPath()
                + "\n\tOutput path: " + properties.outputPath()
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
        System.err.println("Usage: " +
                "\n\tmatrixer --target <path> [--output <path>] [--git <URL>]");
    }

}
