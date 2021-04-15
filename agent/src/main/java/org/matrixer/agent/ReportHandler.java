package org.matrixer.agent;

import java.io.IOException;

/**
 * Finds the test case of the invocation report and uses the writer to
 * write the invocation line.
 */
class ReportHandler implements Runnable {

    private SynchronizedWriter writer;
    private InvocationReport report;

    ReportHandler(InvocationReport report, SynchronizedWriter writer) {
        this.writer = writer;
        this.report = report;
    }

    public void run() {
        Invocation invocation = Invocation.fromReport(report);
        writeLine(invocation.asLine());
    }

    void writeLine(String line) {
        try {
            writer.writeLine(line);
        } catch (IOException e) {
            System.out.println("Could not write report: " + e.getMessage());
        }
    }
}
