package org.matrixer;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * Properties parses and store the application properties
 */
class Properties {

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
    final static String DEFAULT_OUTDIR = "matrix-cov";

    Path targetPath;
    Path outputPath;
    URI remoteURL = null;
    String failureReason = "Properties not parsed";

    /**
     * Parses command line arguments, and stores the properties. Parsing
     * will stop at first failure in which case isValid() will return false
     * and reasonForFailure() will return the a string describing why the
     * operation failed.
     *
     * @param args the command line arguments
     */
    void parse(String[] args) {
        Observable.fromArray(args)
                .buffer(2, 2)
                .subscribe(this::parseFlag, this::handleError, () -> {
                    validate();
                    if (isValid()) {
                        applyDefaults();
                    }
                });

    }

    private void validate() {
        if (targetPath == null) {
            setError("Target directory is required");
        }
    }

    private void applyDefaults() {
        if (outputPath == null) {
            outputPath = defaultOutputPath();
        }
    }

    Path defaultOutputPath() {
        return Paths.get(targetPath + File.separator + DEFAULT_OUTDIR);
    }

    private void parseFlag(List<String> flagPair) {
        failureReason = null;
        if (flagPair.size() != 2) {
            throw new IllegalArgumentException("Invalid argument: " + flagPair);
        }
        String flag = flagPair.get(0);
        String arg = flagPair.get(1);
        switch (flag) {
            case TARGET_FLAG:
                targetPath = Paths.get(arg);
                break;
            case OUTDIR_FLAG:
                outputPath = Paths.get(arg);
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
    boolean isValid() {
        return failureReason == null;
    }

    /**
     * If the last parse failed this method returns a description of why it
     * failed.
     *
     * @returns The reason for the last parse failure
     */
    String reasonForFailure() {
        return failureReason;
    }

    /**
     * @returns the path to the target repository
     */
    Path targetPath() {
        return targetPath;
    }

    /**
     * @returns the path to the output directory.
     */
    Path outputPath() {
        return outputPath;
    }

    /**
     * @returns true if the target repo is a remote repo.
     */
    boolean isRemote() {
        return remoteURL != null;
    }

    /**
     * @returns the URI to the remote repository, or null if not remote
     */
    URI remoteURL() {
        return remoteURL;
    }
}
