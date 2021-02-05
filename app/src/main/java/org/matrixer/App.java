package org.matrixer;

public class App {

    Properties properties;

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
                + "\n\tInput path: " + properties.inputPath()
                + "\n\tOutput path: " + properties.outputPath()
                + "\n\tRemote: " + properties.remoteURL());
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        App app = new App(args);
        app.run();
    }

    static void printUsage() {
        System.err.println("Usage: " +
                "\n\tmatrixer --input <path> --output <path>" +
                "\n\tmatrixer --git <path> --output <path>");
    }

}
