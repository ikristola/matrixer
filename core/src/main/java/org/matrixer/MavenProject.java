package org.matrixer;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.github.djeang.vincerdom.VDocument;
import com.github.djeang.vincerdom.VElement;

class MavenProject extends Project {
    public static final String scriptName = "pom.xml";
    public static final String buildDirName = "target";

    private Path outputDir;

    MavenProject(Properties properties) {
        super(properties);
        setOutputDirOrDefault(properties.outputDir());
    }

    private void setOutputDirOrDefault(Path dir) {
        if (dir != null) {
            this.outputDir = dir;
            return;
        }
        setDefaultOutputDirectory();
    }

    private void setDefaultOutputDirectory() {
        this.outputDir = directory().resolve(Path.of(buildDirName, OUTPUT_DIR_NAME));
    }

    @Override
    Path buildScript() {
        return directory().resolve(scriptName);
    }

    @Override
    Path outputDirectory() {
        return outputDir;
    }

    @Override
    void injectBuildScript(String agentString) {
        InputStream in = getScriptInputStream();
        VDocument doc = VDocument.parse(in)
                .root()
                .get("build")
                .get("plugins")
                .apply((plugins) -> {
                    plugins.getAll("plugin")
                            .stream()
                            .filter((plugin) -> "maven-surefire-plugin"
                                    .equals(plugin.get("artifactId").getText()))
                            .forEach((plugin) -> addAgentConfiguration(plugin, agentString));
                }).__.__.__;
        OutputStream out = getScriptOutputStream();
        doc.print(out);
    }

    void addAgentConfiguration(VElement<?> plugin, String agentString) {
        plugin.get("configuration")
                .make() // If not exits
                .get("useManifestOnlyJar")
                .make()
                .text("false").__
                        .get("argLine")
                        .make()
                        .text(agentString);
    }

    InputStream getScriptInputStream() {
        try {
            return new FileInputStream(buildScript().toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                    "MavenProjectPreparer: Could not read build script " + buildScript());
        }
    }

    OutputStream getScriptOutputStream() {
        try {
            return new FileOutputStream(buildScript().toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                    "MavenProjectPreparer: Could not read build script " + buildScript());
        }
    }

    @Override
    List<String> getTestCommand() {
        String[] cmd = new String[] { "mvn", "test" };
        return Arrays.asList(cmd);
    }
}
