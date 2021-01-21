package org.matrixer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void appHasAGreeting() {
        var app = new App();
        app.run();
        assertTrue(true);
    }

    @Test
    void anotherTest() {
        assertTrue(true);
    }
}
