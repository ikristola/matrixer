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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.matrixer.core.util.FileUtils;

class ElementFactoryTest {

    final static String TMP_DIR = System.getProperty("java.io.tmpdir");

    @Test
    void canGenerateNestedElements() throws IOException {
        final String expected = "<!DOCTYPE html><html><head><title>Title</title></head>" +
                "<body><h1>Header</h1>Body<table><tr><th>TableHeader</th></tr>" +
                "<tr><td>TableCell</td></tr></table></body></html>";

        Path file = FileUtils.createTempFile(Path.of(TMP_DIR));

        String str = ElementFactory.doctype() +
                ElementFactory.html(
                        ElementFactory.head(
                                ElementFactory.title("Title")),
                        ElementFactory.body(
                                ElementFactory.header("Header"),
                                "Body",
                                ElementFactory.table(
                                        ElementFactory.tableRow(
                                                ElementFactory.tableHeader("TableHeader")),
                                        ElementFactory.tableRow(
                                                ElementFactory.tableCell("TableCell")))));

        FileUtils.writeToFile(str, file.toString());

        String fileContents = Files.readString(file);
        assertEquals(expected, fileContents);
    }
}
