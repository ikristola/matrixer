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
