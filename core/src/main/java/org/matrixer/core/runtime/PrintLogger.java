package org.matrixer.core.runtime;

import java.io.PrintStream;

public class PrintLogger implements Logger {

    PrintStream writer;
    String prefix;

    public PrintLogger(PrintStream writer) {
        this.writer = writer;
        this.prefix = "";
    }

    public PrintLogger(PrintStream writer, String prefix) {
        this.writer = writer;
        this.prefix = prefix;
    }

    @Override
    public synchronized void log(String msg) {
        writer.println(prefix + ":INFO: "+ msg);
    }

    @Override
    public synchronized void logError(String msg) {
        writer.println(prefix + ":ERROR: "+ msg);
    }

    @Override
    public synchronized void logException(Throwable e) {
        if (e == null) {
            writer.println(prefix + " Logged null exception");
            return;
        }
        if (e.getMessage() != null) {
            writer.println(prefix + ":EXCEPTION: " + e.getMessage());
        }
        e.printStackTrace(writer);
    }

}
