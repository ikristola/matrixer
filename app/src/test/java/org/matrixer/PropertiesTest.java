package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;

import org.junit.jupiter.api.Test;

class PropertiesTest {

    static final String SEP = File.separator;
    // Any non specific path
    static final String ANY_PATH = SEP + "any" + SEP + "path";
    // Some specific path
    static final String SOME_PATH = SEP + "some" + SEP + "path";
    static final String INVALID_PATH = "\0";

    @Test
    void parseOutputPathFromArgs() {
        Path expected = Paths.get(SOME_PATH);
        String[] args = {"--target", ANY_PATH, "--output", expected.toString()};
        Properties prop = new Properties();

        prop.parse(args);

        assertEquals(expected, prop.outputPath());
    }

    @Test
    void parseInputPathFromArgs() {
        Path expected = Paths.get(SOME_PATH);
        String[] args = {"--target", expected.toString()};
        Properties prop = new Properties();

        prop.parse(args);

        assertEquals(expected, prop.targetPath());
    }

    @Test
    void targetPathMustBeSpecified() {
        String[] args =
                {"--output", ANY_PATH, "--git", "github.com/a/repo.git"};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop);
    }

    @Test
    void defaultOutputPathIfNotSpecified() {
        String[] args =
                {"--target", SOME_PATH, "--git", "github.com/a/repo.git"};
        Properties prop = new Properties();

        prop.parse(args);

        assertEquals(prop.defaultOutputPath(), prop.outputPath());
    }

    @Test
    void parseGitHTTPSLink() {
        String link = "https://pabo1800@bitbucket.org/pabo1800/matrixer.git";
        String[] args = {"--target", ANY_PATH, "--git", link};
        Properties prop = new Properties();

        prop.parse(args);

        assertValidRemote(prop, link);
    }

    @Test
    void parseGitSSHLinkWithExplicitProtocol() {
        String link = "ssh://git@bitbucket.org:pabo1800/matrixer.git";
        String[] args = {"--target", ANY_PATH, "--git", link};
        Properties prop = new Properties();

        prop.parse(args);

        assertValidRemote(prop, link);
    }

    @Test
    void parseGitSSHLinkWithoutProtocol() {
        String link = "git@bitbucket.org:pabo1800/matrixer.git";
        String[] args = {"--target", ANY_PATH, "--git", link};
        Properties prop = new Properties();

        prop.parse(args);

        assertValidRemote(prop, "ssh://" + link);
    }

    @Test
    void isNotRemoteWithoutGitFlag() {
        String[] args = {"--target", ANY_PATH};
        Properties prop = new Properties();

        prop.parse(args);
        assertFalse(prop.isRemote());
    }

    @Test
    void invalidGitURLThrowsException() {
        String link = "\\/#@£${[]}\\``´!%&/()=?`^*_:;><";
        String[] args = {"--target", ANY_PATH, "--git", link};
        Properties prop = new Properties();
        assertDoesNotThrow(() -> prop.parse(args));
        assertNOTValid(prop);
    }

    @Test
    void catchesInvalidInputPath() {
        String[] args = {"--target", INVALID_PATH};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop);
    }

    @Test
    void catchesInvalidOutputPath() {
        String[] args = {"--output", INVALID_PATH};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop);
    }

    @Test
    void catchesInvalidFlagFirst() {
        String[] args = {"INVALID", "--target", ANY_PATH};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop);
    }

    @Test
    void catchesInvalidFlagAfter() {
        String[] args = {"--target", ANY_PATH, "INVALID"};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop);
    }

    @Test
    void catchesInvalidFlagInBetween() {
        String[] args =
                {"--target", ANY_PATH, "INVALID", "--output", ANY_PATH};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop);
    }

    void assertValidRemote(Properties prop, String url) {
        assertTrue(prop.isValid(), "Not valid");
        assertTrue(prop.isRemote(), "Not remote");
        assertEquals(toURI(url), prop.remoteURL(), "Not equal");
    }

    /**
     * If parsing failed, there must be a description of why it failed.
     */
    void assertNOTValid(Properties prop) {
        assertFalse(prop.isValid());
        assertFalse(prop.reasonForFailure().isEmpty());
    }

    /**
     * If parsing succeded, there must NOT be a description failure.
     */
    void assertValid(Properties prop) {
        assertTrue(prop.isValid());
        assertTrue(prop.reasonForFailure().isEmpty());
    }

    URI toURI(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL: " + e.getMessage());
        }
    }
}
