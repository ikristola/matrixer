package org.matrixer.core.runtime;

 public interface Logger {

    public void log(String msg);

    public void logError(String msg);

    public void logException(Throwable e);

 }
