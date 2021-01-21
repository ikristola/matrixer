package org.matrixer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void appHasAGreeting() {
        runTheTest();
    }

    @Test
    void anotherTest() {
        assertTrue(true);
    }

    void runTheTest() {
        var app = new App();
        app.run();
        assertTrue(true);
    }
}
