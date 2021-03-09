package org.matrixer;

public class DataParser {

    public static String htmlFromData(String rawData) {

        var tableElements = createHTMLTable(rawData);

        StringBuilder sb = new StringBuilder();
        sb.append(ElementFactory.doctype());
        sb.append(ElementFactory.html(
                ElementFactory.head(),
                ElementFactory.body(
                        ElementFactory.table(tableElements)
                )
        ));

        return sb.toString();
    }

    private static String createHTMLTable(String rawData) {
        return "";
    }
}
