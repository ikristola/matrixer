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
        app.hej();
        assertTrue(true);
    }

    @Test
    void testUntested() {
        var app = new App();
        app.unTestedMethod();
        assertTrue(true);
    }

    @Test
    void testCalculator() {
        var calc = new Calculator();
        assertEquals(3, calc.add(1,2));
    }
}
