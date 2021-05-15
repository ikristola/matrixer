package org.matrixer.report;

import java.io.PrintStream;
import java.util.*;

import org.matrixer.core.ExecutionData;

/**
 * Prints a summary of the execution results to a print stream
 */
public class TextSummaryReporter {

    public void reportTo(ExecutionData data, PrintStream out) {
        var methods = data.getAllTargetMethods();
        long methodCount = methods.size();

        if (methodCount <= 0) {
            out.println("No executed methods");
            return;
        }
        int testCount = data.getAllTestCases().size();

        Collection<Integer> depths = data.getCallStackDepths();
        IntSummaryStatistics stats = depths.stream()
                .mapToInt(d -> d)
                .collect(IntSummaryStatistics::new,
                        IntSummaryStatistics::accept,
                        IntSummaryStatistics::combine);

        out.println("Statistics:"
                + "\n\tExecuted methods: " + methodCount
                + "\n\t" + "Executed tests: " + testCount
                + "\n\t" + "Max stack depth: " + stats.getMax()
                + "\n\t" + "Min stack depth: " + stats.getMin()
                + "\n\t" + "Average stack depth: "
                + String.format(Locale.US, "%.1f", stats.getAverage())
                + "\n\t" + "Median stack depth: " + calcMedian(depths));
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
