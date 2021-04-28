package org.matrixer.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * Properties parses and store the application properties
 */
public class Properties {

    /**
     * Used to provide the path to the target project directory. Required
     * flag
     */
    final static String TARGET_FLAG = "--target";

    /**
     * Used to provide the path to a directory where the resulting data will
     * be stored.
     */
    final static String OUTDIR_FLAG = "--output";

    /**
     * Used to provide the URL to a target git repository. The repository
     * will be downloaded to the target directory
     */
    final static String VCS_FLAG = "--git";

    final static String ANALYZE_ONLY_FLAG = "--analyze";

    final static String TARGET_PKG_FLAG = "--pkg";

    final static String TEST_PKG_FLAG = "--testpkg";

    final static String DEBUG_FLAG = "--debug";

    /**
     * The default output directory if none is provided is a subdirectory of
     * the target directory with this name.
     */
    final static String DEFAULT_RESULTS_DIRNAME = "matrix-cov";


    private Path targetDir;
    private Path outputDir;
    private URI remoteURL = null;
    private String targetPkg;
    private String testPkg;
    private boolean debug = false;
    private boolean analyzeOnly = false;
    private String failureReason = "Properties not parsed";

    public static Properties fromArgs(String... args) {
        Properties p = new Properties();
        p.parse(args);
        return p;
    }

    /**
     * Parses command line arguments, and stores the properties. Parsing
     * will stop at first failure in which case isValid() will return false
     * and reasonForFailure() will return the a string describing why the
     * operation failed.
     *
     * @param args
     *            the command line arguments
     */
    public void parse(String... args) {
        Observable.fromArray(args)
                .buffer(2, 2)
                .subscribe(this::parseFlag, this::handleError, this::onComplete);
    }

    private void parseFlag(List<String> flagPair) {
        failureReason = "";
        if (flagPair.size() != 2) {
            throw new IllegalArgumentException("Argument must be --flag <value>: " + flagPair);
        }
        String flag = flagPair.get(0);
        String arg = flagPair.get(1);
        switch (flag) {
            case TARGET_FLAG:
                setTargetDir(Path.of(arg));
                break;
            case OUTDIR_FLAG:
                setOutputDir(Path.of(arg));
                break;
            case VCS_FLAG:
                setRemoteURL(parseURL(arg));
                break;
            case TARGET_PKG_FLAG:
                setTargetPackage(arg);
                break;
            case TEST_PKG_FLAG:
                setTestPackage(arg);
                break;
            case DEBUG_FLAG:
                setDebug(true);
                break;
            case ANALYZE_ONLY_FLAG:
                setTargetDir(Path.of(arg));
                analyzeOnly = true;
                break;
            default:
                throw new IllegalArgumentException("Unknown flag: '" + flag + "'");
        }
    }

    private URI parseURL(String url) {
        try {
            if (url.startsWith("git@")) {
                return new URI("ssh://" + url);
            }
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Not a valid URL: '" + url + "'\n\t" + e.getMessage());
        }
    }

    private void onComplete() {
        validate();
        if (isValid()) {
            applyDefaults();
        }
    }

    private void validate() {
        if (targetDir == null) {
            setError("Target directory is required");
        }
    }

    private void applyDefaults() {
        if (testPkg == null || testPkg.isBlank()) {
            testPkg = targetPkg;
        }
    }

    public static Path defaultOutputPath(Path targetDir, String buildDirName) {
        Path resultsDir = Path.of(buildDirName, DEFAULT_RESULTS_DIRNAME);
        return targetDir.resolve(resultsDir);
    }

    private void handleError(Throwable e) {
        setError(e.getMessage());
    }

    private void setError(String err) {
        failureReason = err;
    }

    /**
     * Returns the status of the last parse operation
     *
     * @returns true if parse was successful, false otherwise
     */
    public boolean isValid() {
        return failureReason.isEmpty();
    }

    /**
     * If the last parse failed this method returns a description of why it
     * failed.
     *
     * @returns The reason for the last parse failure
     */
    public String reasonForFailure() {
        return failureReason;
    }

    /**
     * Sets the path to the target project directory
     */
    public void setTargetDir(Path targetDir) {
        this.targetDir = targetDir;
    }

    /**
     * @returns the path to the target project directory
     */
    public Path targetDir() {
        return targetDir;
    }

    /**
     * Sets the path to the output directory
     */
    public void setOutputDir(Path outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * @returns the path to the output directory.
     */
    public Path outputDir() {
        return outputDir;
    }

    /**
     * @returns true if the target repo is a remote repo.
     */
    public boolean isRemote() {
        return remoteURL != null;
    }

    /**
     * Sets the remote URL for the target project
     */
    public void setRemoteURL(URI remoteURL) {
        this.remoteURL = remoteURL;
    }

    /**
     * @returns the URI to the remote repository, or null if not remote
     */
    public URI remoteURL() {
        return remoteURL;
    }

    /**
     * @returns the name of the target package that should be instrumented
     */
    public String targetPackage() {
        return targetPkg;
    }

    /**
     * Sets the name of the package to instrument
     */
    public void setTargetPackage(String packageName) {
        targetPkg = packageName;
    }

    /**
     * @returns the name of the package containing the test cases
     */
    public String testPackage() {
        return testPkg;
    }

    /**
     * Sets the package name for the test cases
     */
    public void setTestPackage(String packageName) {
        testPkg = packageName;
    }

    public boolean analyzeOnly() {
        return analyzeOnly;
    }

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}
