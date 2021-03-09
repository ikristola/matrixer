package org.matrixer;

import java.util.Arrays;

public class ElementFactory {

    public static String doctype() {
        return "<!DOCTYPE html>";
    }

    public static String html() {
        return html("");
    }

    public static String html(String... inner) {
        return "<html>" + concatenateInnerElements(inner) + "</html>";
    }

    public static String body() {
        return body("");
    }

    public static String body(String... inner) {
        return "<body>" + concatenateInnerElements(inner) + "</body>";
    }

    public static String main() {
        return main("");
    }

    public static String main(String... inner) {
        return "<main>" + concatenateInnerElements(inner) + "</main>";
    }

    public static String header() {
        return header("");
    }

    public static String header(String... inner) {
        return "<h1>" + concatenateInnerElements(inner) + "</h1>";
    }

    public static String head() {
        return head("");
    }

    public static String head(String... inner) {
        return "<head>" + concatenateInnerElements(inner) + "</head>";
    }

    public static String title() {
        return title("");
    }

    public static String title(String... inner) {
        return "<title>" + concatenateInnerElements(inner) + "</title>";
    }

    public static String table() {
        return table("");
    }

    public static String table(String... inner) {
        return "<table>" + concatenateInnerElements(inner) + "</table>";
    }

    public static String tableHeader() {
        return tableHeader("");
    }

    public static String tableHeader(String... inner) {
        return "<th>" + concatenateInnerElements(inner) + "</th>";
    }

    public static String tableRow() {
        return tableRow("");
    }

    public static String tableRow(String... inner) {
        return "<tr>" + concatenateInnerElements(inner) + "</tr>";
    }

    public static String tableCell() {
        return tableCell("");
    }

    public static String tableCell(String... inner) {
        return "<td>" + concatenateInnerElements(inner) + "</td>";
    }

    private static String concatenateInnerElements(String... inner) {
        StringBuilder sb = new StringBuilder();
        for (String string : inner) {
            sb.append(string);
        }
        return sb.toString();
    }
}
