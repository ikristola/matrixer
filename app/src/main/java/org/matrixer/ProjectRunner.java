package org.matrixer;

import java.io.IOException;

/**
 * Executes the test suite of target Gradle-based java project
 */
public class ProjectRunner {

    void run (String targetProjectPath) throws IOException {

        // Run target project
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(
                "gradle test -p " + targetProjectPath);
        System.out.println("Successfully started process\n");
    }
}
