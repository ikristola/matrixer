package org.matrixer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class PropertiesTest {

    @Test
    void parseOutputPathFromArgs() {
        Path expected = Paths.get("/some/path");
        String[] args = {"--output", expected.toString()};
        Properties prop = new Properties();

        prop.parse(args);

        assertTrue(prop.isValid());
        assertEquals(expected, prop.outputPath());
    }

    @Test
    void parseInputPathFromArgs() {
        Path expected = Paths.get("/some/path");
        String[] args = {"--input", expected.toString()};
        Properties prop = new Properties();

        prop.parse(args);

        assertTrue(prop.isValid());
        assertEquals(expected, prop.inputPath());
    }

    @Test
    void parseGitHTTPSLink() {
        String link = "https://pabo1800@bitbucket.org/pabo1800/matrixer.git";
        String[] args = {"--git", link};
        Properties prop = new Properties();

        prop.parse(args);

        assertTrue(prop.isValid(), "Not valid");
        assertTrue(prop.isRemote(), "Not remote");
        assertEquals(toURI(link), prop.remoteURL(), "Not equal");
    }

    @Test
    void parseGitSSHLinkWithExplicitProtocol() {
        String link = "ssh://git@bitbucket.org:pabo1800/matrixer.git";
        String[] args = {"--git", link};
        Properties prop = new Properties();

        prop.parse(args);

        assertTrue(prop.isValid(), "Not valid");
        assertTrue(prop.isRemote(), "Not remote");
        assertEquals(toURI(link), prop.remoteURL(), "Not equal");
    }

    @Test
    void parseGitSSHLinkWithoutProtocol() {
        String link = "git@bitbucket.org:pabo1800/matrixer.git";
        String[] args = {"--git", link};
        Properties prop = new Properties();

        prop.parse(args);

        assertTrue(prop.isValid(), "Not valid");
        assertTrue(prop.isRemote(), "Not remote");
        assertEquals(toURI("ssh://" + link), prop.remoteURL(), "Not equal");
    }

    @Test
    void catchesInvalidInputPath() {
        String[] args = {"--input", "\0"};
        Properties prop = new Properties();

        prop.parse(args);

        assertFalse(prop.isValid());
    }

    @Test
    void catchesInvalidOutputPath() {
        String[] args = {"--output", "\0"};
        Properties prop = new Properties();

        prop.parse(args);

        assertFalse(prop.isValid());
        assertFalse(prop.reasonForFailure().isEmpty());
    }

    @Test
    void catchesInvalidFlagFirst() {
        String[] args = {"INVALID", "--input", "/a/path"};
        Properties prop = new Properties();

        prop.parse(args);

        assertFalse(prop.isValid());
        assertFalse(prop.reasonForFailure().isEmpty());
    }

    @Test
    void catchesInvalidFlagAfter() {
        String[] args = {"--input", "/a/path", "INVALID"};
        Properties prop = new Properties();

        prop.parse(args);

        assertFalse(prop.isValid());
        assertFalse(prop.reasonForFailure().isEmpty());
    }

    @Test
    void catchesInvalidFlagInBetween() {
        String[] args = {"--input", "/a/path", "INVALID", "--output", "/a/path"};
        Properties prop = new Properties();

        prop.parse(args);

        assertFalse(prop.isValid());
        assertFalse(prop.reasonForFailure().isEmpty());
    }

    URI toURI(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL: " + e.getMessage());
        }
    }
}
