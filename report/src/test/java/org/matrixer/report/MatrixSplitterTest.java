package org.matrixer.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.matrixer.core.ExecutionData;
import org.matrixer.core.runtime.MethodCall;

public class MatrixSplitterTest {

    @Test
    void diagonalMatrix() {
        // A B C D E
        // 1 x - - - -
        // 2 - x - - -
        // 3 - - x - -
        // 4 - - - x -
        // 5 - - - - x
        MethodCall[] calls = {
                new MethodCall(1, "Method1", "TestCaseA"),
                new MethodCall(1, "Method2", "TestCaseB"),
                new MethodCall(1, "Method3", "TestCaseC"),
                new MethodCall(1, "Method4", "TestCaseD"),
                new MethodCall(1, "Method5", "TestCaseE"),
        };
        ExecutionData data = new ExecutionData();
        for (var c : calls) {
            data.addCall(c);
        }

        var splitter = new MatrixSplitter(data);
        var results = splitter.partition();

        assertEquals(5, results.size());
        for (var r : results) {
            assertEquals(1 * 1, r.matrixSize(), "Matrix:\n" + r);
        }
    }

    @Test
    void matrixWithTwoChunks() {
        // A B C D
        // 1 x x - -
        // 2 x x - -
        // 3 - - x x
        // 4 - - x x
        MethodCall[] calls = {
                new MethodCall(1, "Method1", "TestCaseA"),
                new MethodCall(1, "Method1", "TestCaseB"),
                new MethodCall(1, "Method2", "TestCaseA"),
                new MethodCall(1, "Method2", "TestCaseB"),
                new MethodCall(1, "Method3", "TestCaseC"),
                new MethodCall(1, "Method3", "TestCaseD"),
                new MethodCall(1, "Method4", "TestCaseC"),
                new MethodCall(1, "Method4", "TestCaseD"),
        };
        ExecutionData data = new ExecutionData();
        for (var c : calls) {
            data.addCall(c);
        }

        var splitter = new MatrixSplitter(data);
        var results = splitter.partition();

        assertEquals(2, results.size());
        for (var r : results) {
            assertEquals(2 * 2, r.matrixSize(), "Matrix:\n" + r);
        }
    }

    @Test
    void crossPatternMatrix() {
        // A B C D E
        // 1 x - - - x
        // 2 - x - x -
        // 3 - - x - -
        // 4 - x - x -
        // 5 x - - - x
        MethodCall[] calls = {
                new MethodCall(1, "Method1", "TestCaseA"),
                new MethodCall(1, "Method1", "TestCaseE"),
                new MethodCall(1, "Method2", "TestCaseB"),
                new MethodCall(1, "Method2", "TestCaseD"),
                new MethodCall(1, "Method3", "TestCaseC"),
                new MethodCall(1, "Method4", "TestCaseB"),
                new MethodCall(1, "Method4", "TestCaseD"),
                new MethodCall(1, "Method5", "TestCaseA"),
                new MethodCall(1, "Method5", "TestCaseE"),
        };
        ExecutionData data = new ExecutionData();
        for (var c : calls) {
            data.addCall(c);
        }

        var splitter = new MatrixSplitter(data);
        var results = splitter.partition();

        assertEquals(3, results.size());
        var sizes = new ArrayList<Integer>();
        sizes.add(4);
        sizes.add(4);
        sizes.add(1);

        var count = 0;
        for (var r : results) {
            var it = sizes.iterator();
            while (it.hasNext()) {
                var sz = it.next();
                if (sz == r.matrixSize()) {
                    count++;
                    it.remove();
                    break;
                }
            }
        }

        assertTrue(sizes.isEmpty() && count == results.size(), "Wrong sizes in one of \n" + results.get(0) + "or \n"
                + results.get(1) + "or \n" + results.get(2));
    }
}
