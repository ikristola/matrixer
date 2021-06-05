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

        out.println("Statistics:"
                + "\n\tExecuted methods: " + methodCount
                + "\n\t" + "Executed tests: " + testCount
                + "\n\t" + "Max stack depth: " + depthSummary.getMax()
                + "\n\t" + "Min stack depth: " + depthSummary.getMin()
                + "\n\t" + "Average stack depth: "
                + String.format(Locale.US, "%.1f", depthSummary.getAverage())
                + "\n\t" + "Median stack depth: " + calcMedian(depths)
                + "\n\t" + "Average calls per method: "
                + String.format(Locale.US, "%.1f", callCountSummary.getAverage())
                + "\n\t" + "Median calls per method: " + calcMedian(callsPerMethod));
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
