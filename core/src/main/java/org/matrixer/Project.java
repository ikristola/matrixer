package org.matrixer;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

/**
 * Handle for project specific properties
 *
 * The project naming and structure looks like this
 *
 * Defaults --------
 * 
 * directory -- buildScript (build.gradle / pom.xml) -- buildDirectory
 * (build / target) -- outputDirectory -- LOF_FILE_NAME --
 * RESULTS_FILE_NAME
 *
 *
 * With specified output directory -------------------------------
 *
 * directory -- buildScript (build.gradle / pom.xml) -- buildDirectory
 * (build / target) ....
 *
 * outputDirectory -- LOF_FILE_NAME -- RESULTS_FILE_NAME
 */
abstract class Project {

    public final static String LOG_FILE_NAME = "matrixer-log.txt";
    public final static String RESULTS_FILE_NAME = "matrixer-results.txt";
    public static final String OUTPUT_DIR_NAME = "matrixer-cov";

    final Properties properties;

    Project(Properties properties) {
        this.properties = properties;
    }

    Path logFile() {
        return outputDirectory().resolve(LOG_FILE_NAME);
    }

    Path resultsFile() {
        return outputDirectory().resolve(RESULTS_FILE_NAME);
    }

    Path directory() {
        return properties.targetDir();
    }

    URI remoteURL() {
        return properties.remoteURL();
    }

    abstract void injectBuildScript(String agentString);

    abstract Path buildScript();

    abstract Path outputDirectory();

    abstract List<String> getTestCommand();
}
