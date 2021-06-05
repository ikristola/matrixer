/**
 * Copyright 2021 Patrik Bogren, Isak Kristola
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.matrixer.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.github.djeang.vincerdom.VDocument;
import com.github.djeang.vincerdom.VElement;

import org.matrixer.core.util.FileUtils;

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
    public Path buildScript() {
        return directory().resolve(scriptName);
    }

    @Override
    protected Path _outputDirectory() {
        return outputDir;
    }

    @Override
    void injectBuildScript(String agentString) {
        // Parent project
        injectBuildScript(buildScript(), agentString);
        // All subprojects
        for (Path file : FileUtils.findFiles(directory(), scriptName)) {
            injectBuildScript(file, agentString);
        }
    }

    void injectBuildScript(Path buildScript, String agentString) {
        InputStream in = getInputStream(buildScript);
        VDocument doc = VDocument.parse(in);
        injectSurefirePlugin(doc, agentString);
        OutputStream out = getOutputStream(buildScript);
        doc.print(out);
    }

    void injectSurefirePlugin(VDocument doc, String agentString) {
        var plugins = doc
                .root()
                .get("build")
                .get("plugins");
        var surefire = getOrCreatePlugin(plugins, "maven-surefire-plugin");
        addAgentConfiguration(surefire, agentString);
    }

    <T> VElement<VElement<T>> getOrCreatePlugin(VElement<T> plugins, String pluginName) {
        return plugins.getAll("plugin")
                .stream()
                .filter(pl -> pluginName.equals(pl.get("artifactId").getText()))
                .findFirst()
                .orElseGet(() -> addPlugin(plugins, pluginName));
    }

    <T> VElement<VElement<T>> addPlugin(VElement<T> parent, String pluginName) {
        return parent.add("plugin")
                .add("artifactId")
                    .text(pluginName)
                .__;
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

    InputStream getInputStream(Path file) {
        try {
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(
                    "MavenProjectPreparer: Could not read build script " + buildScript());
        }
    }

    OutputStream getOutputStream(Path file) {
        try {
            return Files.newOutputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(
                    "MavenProjectPreparer: Could not read build script " + buildScript());
        }
    }

    @Override
    List<String> getTestCommand() {
        String[] cmd = new String[] {"mvn", "test"};
        return Arrays.asList(cmd);
    }

    @Override
    public String targetPackage() {
        if (properties.targetPackage() == null) {
            throw new RuntimeException("Target package must be specified for maven projects");
        }
        return properties.targetPackage();
    }

    @Override
    public String testPackage() {
        if (properties.testPackage() == null) {
            return targetPackage();
        }
        return properties.testPackage();
    }
}
