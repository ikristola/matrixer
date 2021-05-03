package org.matrixer.core.runtime;

import java.io.PrintStream;

public class Logger {

    PrintStream writer;
    String prefix;

    public Logger(PrintStream writer) {
        this.writer = writer;
        this.prefix = "";
    }

    public Logger(PrintStream writer, String prefix) {
        this.writer = writer;
        this.prefix = prefix;
    }

    public synchronized void log(String msg) {
        writer.println(prefix + ":INFO:"+ msg);
    }

    public synchronized void logError(String msg) {
        writer.println(prefix + ":ERROR:"+ msg);
    }

    public synchronized void logException(Throwable e) {
        if (e == null) {
            writer.println(prefix + " Logged null exception");
            return;
        }
        if (e.getMessage() != null) {
            writer.println(prefix + ":EXCEPTION:" + e.getMessage());
        }
        e.printStackTrace(writer);
    }

}
