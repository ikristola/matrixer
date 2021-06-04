package org.matrixer.agent;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A wrapper class around a Writer that makes the writes atomic
 */
public class SynchronizedWriter extends BufferedWriter {
    private static String newLine = System.lineSeparator();

    Lock lock = new ReentrantLock();

    public SynchronizedWriter(final Writer writer) {
        super(writer);
    }

    public SynchronizedWriter(final Writer writer, final int size) {
        super(writer, size);
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    @Override
    public void close() throws IOException {
        lock();
        try {
            super.close();
        } finally {
            unlock();
        }
    }

    @Override
    public void flush() throws IOException {
        lock();
        try {
            super.flush();
        } finally {
            unlock();
        }
    }

    @Override
    public void newLine() throws IOException {
        lock();
        try {
            super.newLine();
        } finally {
            unlock();
        }
    }

    @Override
    public void write(final char[] buf, final int off, final int len) throws IOException {
        lock();
        try {
            super.write(buf, off, len);
        } finally {
            unlock();
        }
    }

    @Override
    public void write(final int c) throws IOException {
        lock();
        try {
            super.write(c);
        } finally {
            unlock();
        }
    }

    @Override
    public void write(final String s, final int off, final int len) throws IOException {
        lock();
        try {
            super.write(s, off, len);
        } finally {
            unlock();
        }
    }

    public void writeLine(String s) throws IOException {
        lock();
        try {
            super.write(s + newLine);
            super.flush();
        } finally {
            unlock();
        }
    }

    @Override
    public String toString() {
        lock();
        try {
            return super.toString();
        } finally {
            unlock();
        }
    }

    @Override
    public Writer append(char c) throws IOException {
        lock();
        try {
            return super.append(c);
        } finally {
            unlock();
        }
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        lock();
        try {
            return super.append(csq);
        } finally {
            unlock();
        }
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        lock();
        try {
            return super.append(csq, start, end);
        } finally {
            unlock();
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        lock();
        try {
            return super.clone();
        } finally {
            unlock();
        }
    }
}
