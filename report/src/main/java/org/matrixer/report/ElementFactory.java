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

public class ElementFactory {

    public static String doctype() {
        return "<!DOCTYPE html>";
    }

    public static String html() {
        return html("");
    }

    public static String html(String... inner) {
        return "<html>" + concat(inner) + "</html>";
    }

    public static String body() {
        return body("");
    }

    public static String body(String... inner) {
        return "<body>" + concat(inner) + "</body>";
    }

    public static String main() {
        return main("");
    }

    public static String main(String... inner) {
        return "<main>" + concat(inner) + "</main>";
    }

    public static String style() {
        return style("");
    }

    public static String style(String... inner) {
        return "<style>" + concat(inner) + "</style>";
    }

    public static String header() {
        return header("");
    }

    public static String header(String... inner) {
        return "<h1>" + concat(inner) + "</h1>";
    }

    public static String head() {
        return head("");
    }

    public static String head(String... inner) {
        return "<head>" + concat(inner) + "</head>";
    }

    public static String title() {
        return title("");
    }

    public static String title(String... inner) {
        return "<title>" + concat(inner) + "</title>";
    }

    public static String table() {
        return table("");
    }

    public static String table(String... inner) {
        return "<table>" + concat(inner) + "</table>";
    }

    public static String tableHeader() {
        return tableHeader("");
    }

    public static String tableHeader(String... inner) {
        return "<th>" + concat(inner) + "</th>";
    }

    public static String rotatedTableHeader(String... inner) {
        return "<th class=\"rotate\"><div><span>" + concat(inner) + "</span></div></th>";
    }

    public static String tableRow() {
        return tableRow("");
    }

    public static String tableRow(String... inner) {
        return "<tr>" + concat(inner) + "</tr>";
    }

    public static String tableCell() {
        return tableCell("");
    }

    public static String tableCell(String... inner) {
        return "<td>" + concat(inner) + "</td>";
    }

    private static String concat(String... inner) {
        StringBuilder sb = new StringBuilder();
        for (String string : inner) {
            sb.append(string);
        }
        return sb.toString();
    }
}
