package org.matrixer;

import io.reactivex.rxjava3.core.Observable;

public class App {

    public static void main(String[] args) {
        Observable.just("App: ", "works!", "\n")
                .subscribe(System.out::print);
    }
}
