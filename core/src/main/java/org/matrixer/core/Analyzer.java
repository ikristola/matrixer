package org.matrixer.core;

import java.io.*;

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
        var stream = new BufferedReader(new InputStreamReader(source));
        stream.lines()
                .map(this::parseMethodCall)
                .forEach(data::addCall);
        return data;
    }

    private MethodCall parseMethodCall(String line) {
        int depthIdx = line.indexOf("|");
        int testCaseIdx = line.indexOf("<=");
        int depth = Integer.valueOf(line.substring(0, depthIdx));
        String methodName = line.substring(depthIdx + 1, testCaseIdx);
        String testCaseName = line.substring(testCaseIdx + 2);
        return new MethodCall(depth, methodName, testCaseName);
    }
}
