package org.matrixer;

import io.reactivex.rxjava3.core.Observable;
import org.jacoco.core.JaCoCo;

public class App {

    public static void main(String[] args) {
        Observable.just("App: ", "works!", "\n")
                .subscribe(System.out::print);
        testJaCoco();
    }

    public static void testJaCoco() {
        System.out.println("\n::JaCoCo::"
                + "\n\tversion: " + JaCoCo.VERSION
                + "\n\turl: " + JaCoCo.HOMEURL
                + "\n\truntime: " + JaCoCo.RUNTIMEPACKAGE);
    }

}
