package org.matrixer.core;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PropertiesTest {

    // Any non specific path
    static final Path ANY_PATH = Path.of("any", "path");
    // Some specific path
    static final Path SOME_PATH = Path.of("some", "path");
    static final String INVALID_PATH = "\0";
    static final String INVALID_URL = "\\/#@£${[]}\\``´!%&/()=?`^*_:;><";

    @Test
    void canSetAnalyzeOnlyToSkipRunningStep() {
        Path projectDir = ANY_PATH;
        String[] args = {"--analyze", projectDir.toString()};
        Properties prop = new Properties();

        prop.parse(args);

        assertValid(prop);
        assertTrue(prop.shouldAnalyze());
        assertFalse(prop.shouldRun());
        assertEquals(projectDir, prop.targetDir());
    }

    @Test
    void parseOutputPathFromArgs() {
        Path expected = SOME_PATH;
        String[] args = {"--target", ANY_PATH.toString(), "--output", expected.toString()};
        Properties prop = new Properties();

        prop.parse(args);

        assertEquals(expected, prop.outputDir());
    }

    @Test
    void parseInputPathFromArgs() {
        Path expected = SOME_PATH;
        String[] args = {"--target", expected.toString()};
        Properties prop = new Properties();

        prop.parse(args);

        assertEquals(expected, prop.targetDir());
    }

    @Test
    void targetPathMustBeSpecified() {
        String[] args = {"--output", ANY_PATH.toString(), "--git", "github.com/a/repo.git"};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop, "Target");
    }

    @Test
    void outputPathIsNullIfNotSpecified() {
        String[] args = {"--target", SOME_PATH.toString(), "--git", "github.com/a/repo.git"};
        Properties prop = new Properties();

        prop.parse(args);

        assertEquals(null, prop.outputDir());
    }

    @Test
    void parseGitHTTPSLink() {
        String link = "https://pabo1800@bitbucket.org/pabo1800/matrixer.git";
        String[] args = {"--target", ANY_PATH.toString(), "--git", link};
        Properties prop = new Properties();

        prop.parse(args);

        assertValidRemote(prop, link);
    }

    @Test
    void parseGitSSHLinkWithExplicitProtocol() {
        String link = "ssh://git@bitbucket.org:pabo1800/matrixer.git";
        String[] args = {"--target", ANY_PATH.toString(), "--git", link};
        Properties prop = new Properties();

        prop.parse(args);

        assertValidRemote(prop, link);
    }

    @Test
    void parseGitSSHLinkWithoutProtocol() {
        String link = "git@bitbucket.org:pabo1800/matrixer.git";
        String[] args = {"--target", ANY_PATH.toString(), "--git", link};
        Properties prop = new Properties();

        prop.parse(args);

        assertValidRemote(prop, "ssh://" + link);
    }

    @Test
    void isNotRemoteWithoutGitFlag() {
        String[] args = {"--target", ANY_PATH.toString()};
        Properties prop = new Properties();

        prop.parse(args);
        assertFalse(prop.isRemote());
    }

    @Test
    void catchesInvalidGitLink() {
        String link = INVALID_URL;
        String[] args = {"--target", ANY_PATH.toString(), "--git", link};
        Properties prop = new Properties();
        assertDoesNotThrow(() -> prop.parse(args));
        assertNOTValid(prop, "URL");
    }

    @Test
    void catchesInvalidInputPath() {
        String[] args = {"--target", INVALID_PATH};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop, "path");
    }

    @Test
    void catchesInvalidOutputPath() {
        String[] args = {"--output", INVALID_PATH};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop, "path");
    }

    @Test
    void catchesInvalidFlagFirst() {
        String[] args = {"INVALID", "--target", ANY_PATH.toString()};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop, "Unknown flag");
    }

    @Test
    void catchesInvalidFlagAfter() {
        String[] args = {"--target", ANY_PATH.toString(), "INVALID"};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop, "Argument");
    }

    @Test
    void catchesInvalidFlagInBetween() {
        String[] args =
                {"--target", ANY_PATH.toString(), "INVALID", "--output", ANY_PATH.toString()};
        Properties prop = new Properties();

        prop.parse(args);

        assertNOTValid(prop, "Unknown flag");
    }

    @Test
    void canSetTargetPackage() {
        String pkg = "org.matrixer";
        String[] args = {
                "--target", ANY_PATH.toString(),
                "--pkg", pkg
        };
        Properties prop = new Properties();

        prop.parse(args);
        assertEquals(pkg, prop.targetPackage());
    }

    @Test
    void canSetTestPackage() {
        String testpkg = "org.matrixer.test";
        String[] args = {
                "--target", ANY_PATH.toString(),
                "--testpkg", testpkg
        };
        Properties prop = new Properties();

        prop.parse(args);
        assertEquals(testpkg, prop.testPackage());
    }

    @Test
    void canSetDebug() {
        Properties properties = new Properties();
        properties.setDebug(true);
        assertTrue(properties.getDebug());
    }

    @Test
    void canParseDebug() {
        String[] args = {
                "--debug", "true",
        };
        Properties properties = new Properties();
        properties.parse(args);
        assertEquals(true, properties.getDebug());
    }

    @Test
    void canParseDebugFalse() {
        String[] args = {
                "--debug", "false",
        };
        Properties properties = new Properties();
        properties.parse(args);
        assertEquals(false, properties.getDebug());
    }

    @Test
    void invalidIfNonBooleanDebug() {
        String[] args = {
                "--debug", "ss",
        };
        Properties properties = new Properties();
        properties.parse(args);
        assertNOTValid(properties, "Debug");
    }

    @Test
    void canParseDepthLimit() {
        String[] args = {
                "--depth", "555",
        };
        Properties properties = new Properties();
        properties.parse(args);
        assertEquals(555, properties.getDepthLimit());
    }

    @Test
    void InvalidIfInvalidInteger() {
        String[] args = {
                "--depth", "ss",
        };
        Properties properties = new Properties();
        properties.parse(args);
        assertNOTValid(properties, "Depth");
    }

    @Test
    void canSkipInstrumentBuildSScript() {
        String[] args = {
                "--skip-instrument", "true",
        };
        Properties properties = new Properties();
        properties.parse(args);
        assertFalse(properties.shouldInstrument());
    }

    @Test
    void doesNotSkipInstrumentBuildSScriptIfFalse() {
        String[] args = {
                "--skip-instrument", "false",
        };
        Properties properties = new Properties();
        properties.parse(args);
        assertTrue(properties.shouldInstrument());
    }

    @Test
    void canSetDepthLimit() {
        Properties properties = new Properties();
        properties.setDepthLimit(42);
        assertEquals(42, properties.getDepthLimit());
    }

    void assertValidRemote(Properties prop, String url) {
        assertTrue(prop.isValid(), "Not valid");
        assertTrue(prop.isRemote(), "Not remote");
        assertEquals(toURI(url), prop.remoteURL(), "Not equal");
    }

    /**
     * If parsing failed, there must be a description of why it failed.
     */
    void assertNOTValid(Properties prop, String errorHint) {
        assertFalse(prop.isValid(), "Unexpected valid properties");
        assertFalse(prop.reasonForFailure().isEmpty(), "Unexpected empty error message");

        String errMsg = prop.reasonForFailure();
        assertTrue(errMsg.contains(errorHint),
                "Error message '" + errMsg + "' did not contain hint: " + errorHint);
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
