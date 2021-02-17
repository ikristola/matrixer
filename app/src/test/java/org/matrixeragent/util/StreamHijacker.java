package org.matrixeragent.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Utility class for capturing data written to System.out
 */
public class StreamHijacker {

    private final PrintStream standardOut;
    private final ByteArrayOutputStream outputStreamCaptor;

    public StreamHijacker() {
        standardOut = System.out;
        outputStreamCaptor = new ByteArrayOutputStream();
    }

    public void outputCapture() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    public String getHijackedOutput() {
        return outputStreamCaptor.toString().trim();
    }

    public void stopOutputCapture() {
        reset();
        System.setOut(standardOut);
    }

    public void reset() {
        outputStreamCaptor.reset();
    }
}
