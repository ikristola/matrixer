package org.matrixer.core.runtime;

 public class NoopLogger implements Logger {

    @Override
    public void log(String msg) {
        // No-op
    }

    @Override
    public void logError(String msg) {
        // No-op
    }

    @Override
    public void logException(Throwable e) {
        // No-op
    }

 }
