package org.matrixer.core;

import java.io.*;

import org.matrixer.core.runtime.MethodCall;

/**
 * Analyzes data collected with the matrixer agent
 */
public class Analyzer {

    /**
     * Parses execution data from source
     *
     * @param source
     *            A stream containing execution data from the agent
     *
     * @returns the aggregated execution data
     */
    public ExecutionData analyze(InputStream source) {
        ExecutionData data = new ExecutionData();
        BufferedReader stream = new BufferedReader(new InputStreamReader(source));
        stream.lines()
                .filter(line -> line != null && !line.isBlank())
                .map(MethodCall::new)
                .filter(call -> call != null)
                .forEach(data::addCall);
        return data;
    }
}
