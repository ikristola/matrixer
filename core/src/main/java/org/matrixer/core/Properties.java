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

    /**
     * The default output directory if none is provided is a subdirectory of
     * the target directory with this name.
     */
    final static String DEFAULT_RESULTS_DIRNAME = "matrix-cov";

    private Path targetDir;
    private Path outputDir;
    private String buildDirName = "build";
    private URI remoteURL = null;
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
     * @param args the command line arguments
     */
    public void parse(String... args) {
        Observable.fromArray(args)
                .buffer(2, 2)
                .subscribe(this::parseFlag, this::handleError, this::onComplete);
    }

    private void parseFlag(List<String> flagPair) {
        failureReason = null;
        if (flagPair.size() != 2) {
            throw new IllegalArgumentException("Argument must be --flag <value>: " + flagPair);
        }
        String flag = flagPair.get(0);
        String arg = flagPair.get(1);
        switch (flag) {
            case TARGET_FLAG:
                targetDir = Paths.get(arg);
                break;
            case OUTDIR_FLAG:
                outputDir = Paths.get(arg);
                break;
            case VCS_FLAG:
                remoteURL = parseURL(arg);
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
        if (outputDir == null) {
            outputDir = defaultOutputPath(targetDir, buildDirName);
        }
    }

    public void setBuildDirName(String buildDirName) {
        this.buildDirName = buildDirName;
    }

    public Path defaultOutputDir() {
        return defaultOutputPath(targetDir, buildDirName);
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
        return failureReason == null;
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

    public void setTargetDir(Path targetDir) {
        this.targetDir = targetDir;
    }

    /**
     * @returns the path to the target repository
     */
    public Path targetDir() {
        return targetDir;
    }

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

    public void setRemoteURL(URI remoteURL) {
        this.remoteURL = remoteURL;
    }

    /**
     * @returns the URI to the remote repository, or null if not remote
     */
    public URI remoteURL() {
        return remoteURL;
    }
}
