package org.matrixer.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.matrixer.core.*;
import org.matrixer.core.util.GitRepository;
import org.matrixer.report.HTMLReporter;
import org.matrixer.report.TextSummaryReporter;

public class App {

    /**
     * The name of the file containing the html report
     */
    public static String HTML_REPORT_FILENAME = "matrixer-report.html";

    Properties properties;
    GitRepository repo;
    Project project;
    ExecutionData data;

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
        project = prepareProject();

        if (properties.instrumentOnly()) {
            return;
        }

        if (!properties.analyzeOnly()) {
            int status = runProject();
            if (status != 0) {
                throw new RuntimeException("Target project tests exited with error(" + status
                        + ") see logfile for details");
            }
            System.out.println("Target project tests was run successfully!");
        }

        data = analyzeProject();
        printSummary(data, System.out);
        generateHTMLReport(data);
    }

    private Project prepareProject() throws GitAPIException, IOException {
        System.out.println("Preparing target project: " + properties.targetDir());
        ProjectPreparer preparer = new ProjectPreparer();
        return preparer.prepare(properties);
    }

    private int runProject() {
        System.out.println("Running target project tests");
        ProjectRunner runner = new ProjectRunner();
        return runner.runTests(project);
    }

    private ExecutionData analyzeProject() throws IOException {
        Path file = project.resultsFile();
        System.out.println("Analyzing results in " + file);
        if (!Files.exists(file)) {
            throw new RuntimeException("File did not exist: " + file);
        }
        try (var in = Files.newInputStream(file, StandardOpenOption.TRUNCATE_EXISTING)) {
            Analyzer analyzer = new Analyzer();
            ExecutionData results = analyzer.analyze(in);
            return results;
        } catch (IOException e) {
            var ex = new IOException("Analyzing " + file + ": " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    private void generateHTMLReport(ExecutionData data) throws IOException {
        System.out.println("Generating html report");
        Path htmlFile = project.outputDirectory().resolve(HTML_REPORT_FILENAME);
        try (var out = Files.newOutputStream(htmlFile)) {
            HTMLReporter reporter = new HTMLReporter(data);
            reporter.reportTo(out);
        } catch (IOException e) {
            var ex = new IOException("Analyzing " + htmlFile + ": " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    private void printSummary(ExecutionData data, PrintStream out) {
        var reporter = new TextSummaryReporter();
        reporter.reportTo(data, out);
    }

    Project getProject() {
        return project;
    }

    ExecutionData getData() {
        return data;
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
                        + "[--testpkg <test package name>] [--output <path>] [--git <URL>]\n"
                        + "or\n"
                        + "\tmatrixer --analyze <path> --pkg <target package name>"
                        + "[--testpkg <test package name>] [--output <path>] [--git <URL>]\n\n\t"
                        + "or\n"
                        + "\tmatrixer --instrument <path> --pkg <target package name>"
                        + "[--testpkg <test package name>] [--output <path>] [--git <URL>]\n\n\t"
                        + "--target  - the location of an existing project or the path to clone the remote repo to\n\t"
                        + "--pkg     - root package name of the target project, will be used to identify target methods\n\t"
                        + "--testpkg - root package name of the tests, will be used to identify test cases (defaults to --pkg)\n\t"
                        + "--output  - the location where logs and results will be stored. Defaults to build/matrix-cov for gradle and target/matrix-cov for maven\n\t"
                        + "--git     - if the project is remote, provide a URL to the repository\n\t"
                        + "--analyze - skip the running of tests and only analyze existing results\n");
    }

}
