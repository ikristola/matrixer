package org.matrixer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;

import io.reactivex.rxjava3.core.Observable;

public class App {

    public static class MemoryClassLoader extends ClassLoader {
        private final Map<String, byte[]> definitions =
                new HashMap<String, byte[]>();

        public void addDefinition(final String name, final byte[] bytes) {
            definitions.put(name, bytes);
        }

        @Override
        protected Class<?> loadClass(final String name, final boolean resolve)
                throws ClassNotFoundException {
            final byte[] bytes = definitions.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.loadClass(name, resolve);
        }
    }

    public static void main(String[] args) {
        var banana = new App();
        banana.A();
        // app.run();
    }

    void A() {
        System.out.println("A()");
        B();
    }

    void B() {
        System.out.println("B()");
        C();
    }

    void C() {
        System.out.println("C()");
        D();
    }

    void D() {
        System.out.println("D()");
        var stackTraceElements = Thread.currentThread().getStackTrace();

        for (var elem : stackTraceElements) {
            System.out.println(elem);
        }
        var len = stackTraceElements.length;
        var origin = stackTraceElements[len - 2];
        System.out.println("Original caller of D was: " + origin +
                "\nThe class loader was: " + origin.getClassLoaderName() +
                "\nThe class was: " + origin.getClassName() +
                "\nThe method was: " + origin.getMethodName() + 
                "\nThe filename was." + origin.getFileName() +
                "\nThe linenbr was: " + origin.getLineNumber());
    }



    private void run() {
        try {
            instrument();
            analyze("/home/pabo/tmp/matrixer-test/build/classes/");
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    private void instrument() {
        var runtime = new OfflineInstrumentationAccessGenerator();
        var instr = new Instrumenter(runtime);
    }

    private void analyze(String path) throws IOException {
        var execData = new ExecutionDataStore();
        var builder = new CoverageBuilder();
        var analyzer = new Analyzer(execData, builder);

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
                .map(line -> "\t  " + line)
                .startWithItem("\tLines:")
                .doOnNext(line -> System.out.println(line))
                .subscribe(s -> {
                }, System.err::println, System.out::println);
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
