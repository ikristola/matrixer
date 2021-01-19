package org.matrixer;

import java.io.IOException;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;

public class App {

    public static void main(String[] args) {
        var app = new App();
        app.run();
    }

    private void run() {
        try {
            analyze();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    private void analyze() throws IOException {
        var execData = new ExecutionDataStore();
        var builder = new CoverageBuilder();
        var analyzer = new Analyzer(execData, builder);
        var count = analyzer.analyzeAll("/home/pabo/tmp/matrixer-test/build/classes/", null);
        System.out.println("Found " + count + " class files");

        for (var source : builder.getSourceFiles()) {
            System.out.println(source.getName());
        }
    }

}
