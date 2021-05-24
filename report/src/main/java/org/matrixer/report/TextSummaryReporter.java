package org.matrixer.report;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import org.matrixer.core.ExecutedMethod;
import org.matrixer.core.ExecutionData;

/**
 * Prints a summary of the execution results to a print stream
 */
public class TextSummaryReporter {

    private ExecutionData data;

    public TextSummaryReporter(ExecutionData data) {
        this.data = data;
    }

    public void reportTo(PrintStream out) {
        var methods = data.getAllTargetMethods();
        long methodCount = methods.size();

        if (methodCount <= 0) {
            out.println("No executed methods");
            return;
        }
        int testCount = data.getAllTestCases().size();

        Collection<Integer> callsPerMethod = countCallers(methods);
        IntSummaryStatistics callCountSummary = summarize(callsPerMethod);

        Collection<Integer> depths = data.getCallStackDepths();
        IntSummaryStatistics depthSummary = summarize(depths);
        double matrixSize = methodCount * testCount;

        var partitions = partition(data);
        Collection<Integer> partitionSizes = partitions.stream()
            .map(MatrixSplitter.Result::matrixSize)
            .collect(Collectors.toList());
        IntSummaryStatistics partitionSizeSummary = summarize(partitionSizes);
        double disjointAvg = partitionSizeSummary.getAverage();
        double disjointMed = calcMedian(partitionSizes);

        out.println("Statistics:"
                + "\n\tExecuted methods: " + methodCount
                + "\n\t" + "Executed tests: " + testCount
                + "\n\t" + "Matrix size: " + matrixSize
                + "\n\t" + "Disjoint submatrices: " + partitions.size()
                + String.format("\n\tLargest submatrix: %d (%.1f%% of full size)",
                        partitionSizeSummary.getMax(), partitionSizeSummary.getMax() * 100.0D / matrixSize)
                + String.format("\n\tSubmatrices avg size: %.1f (%.1f%% of full size)",
                        disjointAvg, disjointAvg * 100.0D / matrixSize)
                + String.format("\n\tSubmatrices median size: %.1f (%.1f%% of full size)",
                        disjointMed, disjointMed * 100.0D / matrixSize)
                + "\n\t" + "Max stack depth: " + depthSummary.getMax()
                + "\n\t" + "Min stack depth: " + depthSummary.getMin()
                + "\n\t" + "Average stack depth: "
                + String.format(Locale.US, "%.1f", depthSummary.getAverage())
                + "\n\t" + "Median stack depth: " + calcMedian(depths)
                + "\n\t" + "Average calls per method: "
                + String.format(Locale.US, "%.1f", callCountSummary.getAverage())
                + "\n\t" + "Median calls per method: " + calcMedian(callsPerMethod));

        String headers = "depth\tmeth\ttest\tsize\tparts\tpartMax\tpartAvg";
        String tablfmt = " %d\t%d\t%d\t%.1f\t%d\t%d\t%.1f";
        String tableLine = String.format(tablfmt,
                depthSummary.getMax(),
                methodCount,
                testCount, 
                matrixSize,
                partitions.size(),
                partitionSizeSummary.getMax(),
                disjointAvg
        );
        out.println("Headers   : " + headers);
        out.println("Table line: " + tableLine);
    }

    List<MatrixSplitter.Result> partition(ExecutionData data) {
        MatrixSplitter splitter = new MatrixSplitter(data);
        return splitter.partition();
    }

    double getAvgSize(List<MatrixSplitter.Result> partitions) {
        return partitions.stream()
                .mapToInt(MatrixSplitter.Result::matrixSize)
                .average()
                .getAsDouble();
    }

    Collection<Integer> countCallers(Collection<ExecutedMethod> methods) {
        return methods
                .stream()
                .map(m -> m.callers().size())
                .collect(Collectors.toList());
    }

    IntSummaryStatistics summarize(Collection<Integer> depths) {
        return depths
                .stream()
                .mapToInt(d -> d)
                .collect(IntSummaryStatistics::new,
                        IntSummaryStatistics::accept,
                        IntSummaryStatistics::combine);
    }

    double calcMedian(Collection<Integer> ints) {
        MedianFinder finder = new MedianFinder();
        ints.stream().forEach(finder::addNum);
        return finder.findMedian();
    }

    // https://www.programcreek.com/2015/01/leetcode-find-median-from-data-stream-java/
    class MedianFinder {
        PriorityQueue<Integer> minHeap = null;
        PriorityQueue<Integer> maxHeap = null;

        public MedianFinder() {
            minHeap = new PriorityQueue<>();
            maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        }

        public void addNum(int num) {
            minHeap.offer(num);
            maxHeap.offer(minHeap.poll());

            if (minHeap.size() < maxHeap.size()) {
                minHeap.offer(maxHeap.poll());
            }
        }

        public double findMedian() {
            if (minHeap.size() > maxHeap.size()) {
                return minHeap.peek();
            } else {
                return (minHeap.peek() + maxHeap.peek()) / 2.0;
            }
        }
    }

}
