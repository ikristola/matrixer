package org.matrixer.core;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

/**
 * Handle for project specific properties
 *
 * The project naming and structure looks like this
 *
 * Defaults
 *
 * <pre>
 * directory
 * |-- buildScript (build.gradle / pom.xml)
 * |-- buildDirectory (build / target)
 *    |-- outputDirectory
 *       |-- LOF_FILE_NAME
 *       |-- RESULTS_FILE_NAME
 * </pre>
 *
 *
 * With specified output directory
 *
 * <pre>
 * directory
 * |-- buildScript (build.gradle / pom.xml)
 * |-- buildDirectory (build / target)
 * ....
 *
 * outputDirectory
 * |-- LOF_FILE_NAME
 * |-- RESULTS_FILE_NAME
 * </pre>
 */
public abstract class Project {

    public final static String LOG_FILE_NAME = "matrixer-log.txt";
    public final static String RESULTS_FILE_NAME = "matrixer-results.txt";
    public static final String OUTPUT_DIR_NAME = "matrixer-cov";

    protected final Properties properties;

    Project(Properties properties) {
        this.properties = properties;
    }

    public Path logFile() {
        return outputDirectory().resolve(LOG_FILE_NAME);
    }

    public Path resultsFile() {
        return outputDirectory().resolve(RESULTS_FILE_NAME);
    }

    public Path directory() {
        return properties.targetDir();
    }

    public URI remoteURL() {
        return properties.remoteURL();
    }

    public String targetPackage() {
        return properties.targetPackage();
    }

    public String testPackage() {
        return properties.testPackage();
    }

    abstract void injectBuildScript(String agentString);

    public abstract Path buildScript();

    public abstract Path outputDirectory();

    abstract List<String> getTestCommand();
}
