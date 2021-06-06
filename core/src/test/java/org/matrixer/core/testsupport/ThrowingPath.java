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
package org.matrixer.core.testsupport;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;

public class ThrowingPath implements Path {

    @Override
    public int compareTo(Path arg0) {
        return 0;
    }

    @Override
    public boolean endsWith(Path arg0) {
        return false;
    }

    @Override
    public Path getFileName() {
        return null;
    }

    @Override
    public FileSystem getFileSystem() {
        return null;
    }

    @Override
    public Path getName(int arg0) {
        return null;
    }

    @Override
    public int getNameCount() {
        return 0;
    }

    @Override
    public Path getParent() {
        return null;
    }

    @Override
    public Path getRoot() {
        return null;
    }

    @Override
    public boolean isAbsolute() {
        return false;
    }

    @Override
    public Path normalize() {
        return null;
    }

    @Override
    public WatchKey register(WatchService arg0, Kind<?>[] arg1, Modifier... arg2)
            throws IOException {
        return null;
    }

    @Override
    public Path relativize(Path arg0) {
        return null;
    }

    @Override
    public Path resolve(Path arg0) {
        return null;
    }

    @Override
    public boolean startsWith(Path arg0) {
        return false;
    }

    @Override
    public Path subpath(int arg0, int arg1) {
        return null;
    }

    @Override
    public Path toAbsolutePath() {
        return null;
    }

    @Override
    public Path toRealPath(LinkOption... arg0) throws IOException {
        throw new IOException();
    }

    @Override
    public URI toUri() {
        return null;
    }
}
