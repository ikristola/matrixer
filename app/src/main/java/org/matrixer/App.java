package org.matrixer;

import java.io.File;
import java.lang.reflect.Field;

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
        System.err.println("Usage: "
                + "\n\tmatrixer --target <path> [--output <path>] [--git <URL>]");
    }

}
