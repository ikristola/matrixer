package org.matrixer.report;

import java.io.PrintStream;
import java.util.Collections;
import java.util.stream.Collectors;

import org.matrixer.core.ExecutionData;

public class TextReporter {

    ExecutionData data;

    public TextReporter(ExecutionData data) {
        this.data = data;
    }

    public void reportTo(PrintStream out) {
        var methods = data.getAllTargetMethods()
            .stream()
            .sorted()
            .collect(Collectors.toList());
        var tests = data.getAllTestCases()
            .stream()
            .sorted()
            .collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();
        for (var m : methods) {
            for (var t : tests) {
                if (m.wasCalledBy(t)) {
                    var minDepth = m.depthOfCall(t).min();
                    builder.append(minDepth);
                } else {
                    builder.append('-');
                }
            }
            builder.append('\n');
        }
        out.print(builder.toString());
    }

}
