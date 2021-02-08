package org.matrixer;

import io.reactivex.rxjava3.core.Observable;

import java.io.IOException;

public class App {

    public static void main(String[] args) {
        Observable.just("App: ", "works!", "\n")
                .subscribe(System.out::print);

        ProjectRunner projectRunner = new ProjectRunner();
        try {
            // Hardcoded path as of now
            String targetProjectPath = "/Users/macbookpro/programming/skola/vt21-applied/code/matrixer-test";
            projectRunner.run(targetProjectPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
