package org.matrixer;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

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
