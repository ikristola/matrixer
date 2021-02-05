package org.matrixer;

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
    Path inputPath;
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
                .subscribe(this::parseFlag, this::handleError);
    }

    private void parseFlag(List<String> flagPair) {
        failureReason = null;
        if (flagPair.size() != 2) {
            throw new IllegalArgumentException("Invalid argument: " + flagPair);
        }
        String flag = flagPair.get(0);
        String arg = flagPair.get(1);
        switch (flag) {
            case "--input":
                inputPath = Paths.get(arg);
                break;
            case "--output":
                outputPath = Paths.get(arg);
                break;
            case "--git":
                remoteURL = parseURL(arg);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown flag: '" + flag + "'");
        }
    }

    private URI parseURL(String url) {
        try {
            if (url.startsWith("git@")) {
                return new URI("ssh://" + url);
            }
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Not a valid URL: " + url);
        }
    }

    private void handleError(Throwable e) {
        failureReason = e.getMessage();
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
     * If the last parse failed this method returns a description of 
     * why it failed.
     *
     * @returns The reason for the last parse failure
     */
    String reasonForFailure() {
        return failureReason;
    }

    /**
     * @returns the path to the target repository
     */
    Path inputPath() {
        return inputPath;
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
