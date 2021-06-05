/**
 * Copyright 2021 Patrik Bogren, Isak Kristola
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.matrixer.agent.util;

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

    public String getOutput(Runnable runnable) {
        outputCapture();

        runnable.run();

        String output = getHijackedOutput();
        stopOutputCapture();
        return output;
    }

}
