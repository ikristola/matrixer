package org.matrixer.cli;

import org.matrixer.report.Report;

import java.nio.file.Path;

import org.matrixer.core.*;

public class App {

    public static String DEFAULT_REPORT_FNAME = "matrixer-report.html";

    Properties properties;
    GitRepository repo;
    Project project;

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
        System.out.println("Preparing target project");
        ProjectPreparer preparer = new ProjectPreparer();
        project = preparer.prepare(properties);

        System.out.println("Running target project tests");
        ProjectRunner runner = new ProjectRunner();
        int status = runner.runTests(project);

        if (status != 0) {
            System.out.println("Target project tests exited with error: " + status);
            return;
        }
        System.out.println("Target project tests was run successfully!");

        System.out.println("Analyzing results");
        Path results = project.outputDirectory().resolve("matrixer-analyzed.txt");
        Analyzer analyzer = new Analyzer(project.resultsFile(), results);
        analyzer.analyze();

        System.out.println("Generating html report");
        Report report = new Report.Builder()
            .dataFile(results)
            .outputPath(project.outputDirectory())
            .build();
        report.generate();
    }

    Project getProject() {
        return project;
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
                "Usage: " + "\n\tmatrixer --target <path> --pkg <target package name> "
                + "[--testpkg <test package name>] [--output <path>] [--git <URL>]\n\n\t"
                + "--target  - the location of an existing project or the path to clone the remote repo to\n\t"
                + "--pkg     - root package name of the target project, will be used to identify target methods\n\t"
                + "--testpkg - root package name of the tests, will be used to identify test cases (defaults to --pkg)\n\t"
                + "--output  - the location where logs and results will be stored. Defaults to build/matrix-cov for gradle and target/matrix-cov for maven\n\t"
                + "--git     - if the project is remote, provide a URL to the repository\n"
        );
    }

}
