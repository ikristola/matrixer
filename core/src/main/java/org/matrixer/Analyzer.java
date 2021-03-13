package org.matrixer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Analyzes method mapping data
 */
public class Analyzer {

    Path sourceFile;
    Path targetFile;

    // key: String representing an application methods.
    // value: Set of test methods that tests the method represented by the
    // key
    Map<String, Set<String>> mappedMethods;

    Analyzer(Path sourceFile, Path targetFile) {
        this.sourceFile = sourceFile;
        this.targetFile = targetFile;

        mappedMethods = new HashMap<>();
    }

    Analyzer(Path sourceFile) {
        this(sourceFile, sourceFile.getParent().resolve("matrixer-raw-data"));
    }

    /**
     * Analyzes method mapping data from the set source file. Writes
     * formatted results to the set file.
     *
     * @throws IOException
     */
    void analyze() throws IOException {
        Files.lines(sourceFile) // read source data
                .map(l -> l.split("<=")) // split app method from test method
                .forEach(this::addToSet);

        writeResultsToFile();
    }

    private void addToSet(String[] strings) {
        // parse incoming app method and test method strings

        String appMethod = strings[0];
        String testMethod = strings[1];
        var set = new TreeSet<String>();
        set.add(testMethod);

        // if key is not in set: add new entry
        // if key already in set: merge old value(s) and new value
        mappedMethods.merge(appMethod, set, (oldVal, newVal) -> {
            var newSet = new TreeSet<String>();
            newSet.addAll(oldVal);
            newSet.addAll(newVal);
            return newSet;
        });
    }

    private void writeResultsToFile() {
        StringBuilder rawData = new StringBuilder();
        mappedMethods.forEach((appMethod, testMethods) -> {
            rawData.append(appMethod);
            for (String testMethod : testMethods) {
                rawData.append("|").append(testMethod);
            }
            rawData.append("\n");
        });

        FileUtils.writeToFile(rawData.toString(), targetFile.toString());
    }

}
