package org.matrixer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class HtmlGeneratorTest {

    final static Path TMP_DIR = FileUtils.getTempDirPath();

    @Test
    void canApplyStyleSheet() {
        String dataString = "";
        String styleSheet = "body { font-weight: normal; }";

        String expected =
                "<!DOCTYPE html><html><head></head>" +
                        "<style>body { font-weight: normal; }</style>" +
                        "<body><table>" +
                        "<tr><th></th></tr>" +
                        "</table></body>" +
                        "</html>";

        assertDataStringGeneratesExpectedReportWithStyleSheet(dataString, styleSheet, expected);
    }

    @Test
    void canGenerateEmptyReportIfEmptyDataString() {
        String dataString = "";

        String expected =
                "<!DOCTYPE html><html><head></head><style></style>" +
                        "<body><table>" +
                        "<tr><th></th></tr>" +
                        "</table></body>" +
                        "</html>";

        assertDataStringGeneratesExpectedReport(dataString, expected);
    }

    @Test
    void canGenerateReportWithOnlyAppMethods() {
        String dataString = "package.Class.method()\n" +
                "package.Class2.method()";

        String expected =
                "<!DOCTYPE html><html><head></head><style></style>" +
                        "<body><table>" +
                        "<tr><th></th></tr>" +
                        "<tr><td>package.Class.method()</td></tr>" +
                        "<tr><td>package.Class2.method()</td></tr>" +
                        "</table></body>" +
                        "</html>";

        assertDataStringGeneratesExpectedReport(dataString, expected);
    }

    @Test
    void noDuplicateTestMethodsInHeaderRow() {
        String dataString = "package.Class.method()|package.Testclass:testMethod\n" +
                "package2.Class.method()|package.Testclass:testMethod";

        String expected =
                "<!DOCTYPE html><html><head></head><style></style>" +
                        "<body><table>" +
                        "<tr><th></th>" +
                        "<th class='rotate'><div><span>package.Testclass:testMethod</span></div>" +
                        "</th></tr>" +
                        "<tr><td>package.Class.method()</td><td>x</td></tr>" +
                        "<tr><td>package2.Class.method()</td><td>x</td></tr>" +
                        "</table></body>" +
                        "</html>";

        assertDataStringGeneratesExpectedReport(dataString, expected);
    }

    @Test
    void canGenerateReportWithMultipleAppMethodsAndTestMethods() {
        String dataString = "package.Class.method()" +
                "|package.Testclass:testMethod" +
                "|package2.Testclass:testMethod\n" +
                "package.Class2.method()" +
                "|package.Testclass:testMethod";

        String expected =
                "<!DOCTYPE html><html><head></head><style></style>" +
                        "<body><table>" +
                        "<tr><th></th>" +
                        "<th class='rotate'><div><span>package.Testclass:testMethod</span></div>" +
                        "</th>" +
                        "<th class='rotate'><div><span>package2.Testclass:testMethod</span></div>" +
                        "</th></tr>" +
                        "<tr><td>package.Class.method()</td><td>x</td><td>x</td></tr>" +
                        "<tr><td>package.Class2.method()</td><td>x</td><td></td></tr>" +
                        "</table></body>" +
                        "</html>";

        assertDataStringGeneratesExpectedReport(dataString, expected);
    }

    void assertDataStringGeneratesExpectedReport(String dataString, String expected) {
        File file = FileUtils.createTempFile(TMP_DIR);
        FileUtils.writeToFile(dataString, file.toString());

        HtmlGenerator htmlGenerator = new HtmlGenerator(file.toString());
        var reportHtml = htmlGenerator.generateReport();

        assertEquals(expected, reportHtml);
    }


    void assertDataStringGeneratesExpectedReportWithStyleSheet(
            String dataString, String styleSheet, String expected) {
        File styleFile = FileUtils.createTempFile(TMP_DIR);
        FileUtils.writeToFile(styleSheet, styleFile.toString());

        File dataFile = FileUtils.createTempFile(TMP_DIR);
        FileUtils.writeToFile(dataString, dataFile.toString());

        HtmlGenerator htmlGenerator = new HtmlGenerator(dataFile.toString(), styleFile.toString());
        var reportHtml = htmlGenerator.generateReport();

        assertEquals(expected, reportHtml);
    }

}
