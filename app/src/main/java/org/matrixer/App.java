package org.matrixer;

import java.io.IOException;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.data.ExecutionDataStore;

import io.reactivex.rxjava3.core.Observable;

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

        var path = "/home/pabo/tmp/matrixer-test/build/classes/";
        var count = analyzer.analyzeAll(path, null);
        System.out.println("Found " + count + " class files:");

        Observable.fromIterable(builder.getSourceFiles())
                .map(ISourceFileCoverage::getName)
                .subscribe(System.out::println);

        System.out.println("::Classes::");
        Observable.fromIterable(builder.getClasses())
                .filter(cls -> !isTestClass(cls))
                .doOnNext(cls -> System.out.println(cls.getName()))
                .flatMap(cls -> Observable.fromIterable(cls.getMethods()))
                .subscribe(this::printMethod);

    }

    void printMethod(IMethodCoverage method) {
        System.out.println("\tName: " + method.getName());
        System.out.println("\tDesc: " + method.getDesc());

        getMethodLines(method)
                .map(ILine::getStatus)
                .map(this::getStatusString)
                .startWithItem("Lines:")
                .doOnNext(System.out::println)
                .subscribe(s -> {
                }, e -> {
                }, () -> System.out.println());
    }

    Observable<ILine> getMethodLines(IMethodCoverage method) {
        return Observable.create(emitter -> {
            var first = method.getFirstLine();
            var last = method.getLastLine();
            for (int i = first; i <= last; i++) {
                ILine line = method.getLine(i);
                emitter.onNext(line);
            }
            emitter.onComplete();
        });
    }

    boolean isTestClass(IClassCoverage cls) {
        return cls.getName()
                .toLowerCase()
                .endsWith("test");
    }

    String getStatusString(int status) {
        switch (status) {
            case ICounter.EMPTY:
                return "Empty";
            case ICounter.FULLY_COVERED:
                return "Fully covered";
            case ICounter.NOT_COVERED:
                return "Not covered";
            case ICounter.PARTLY_COVERED:
                return "Partly covered";
            default:
                throw new RuntimeException("Not a valid status: " + status);
        }
    }

}
