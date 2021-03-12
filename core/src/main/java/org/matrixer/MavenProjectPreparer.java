package org.matrixer;

import java.io.*;
import java.nio.file.Path;

import com.github.djeang.vincerdom.VDocument;
import com.github.djeang.vincerdom.VElement;

class MavenProjectPreparer implements ProjectPreparer {

    public final static String BUILD_DIR_NAME = "target";

    private final Path buildScript;
    private final Path targetPath;
    private final Path outputPath;

    public MavenProjectPreparer(Path targetpath) {
        this(targetpath, Properties.defaultOutputPath(targetpath, BUILD_DIR_NAME));
    }

    public MavenProjectPreparer(Path targetPath, Path outputPath) {
        this.targetPath = targetPath;
        this.outputPath = outputPath;
        this.buildScript = targetPath.resolve(MAVEN_BUILD_FILE_NAME);
    }

    @Override
    public BuildType buildType() {
        return BuildType.Maven;
    }

    @Override
    public Path getBuildScript() {
        return buildScript;
    }

    @Override
    public void prepare() {
        injectBuildScript();
    }

    String buildJavaAgentString() {
        String rootPkg = "org.matrixertest";
        String CWD = System.getProperty("user.dir");
        Path jarPath = Path.of(CWD, "../agent/build/libs/agentJar.jar").normalize();

        return buildJavaAgentString(jarPath, outputPath, rootPkg, rootPkg);
    }

    // TODO place this function somewhere neat and reachable
    String buildJavaAgentString(Path jarPath, Path outputPath, String targetPkg, String testPkg) {
        return String.format("-javaagent:%s=%s:%s:%s", jarPath, outputPath, targetPkg, targetPkg);

    }

    void injectBuildScript() {
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
                            .forEach(this::addAgentConfiguration);
                }).__.__.__;
        OutputStream out = getScriptOutputStream();
        doc.print(out);
    }

    void addAgentConfiguration(VElement<?> plugin) {
        String agentLine = buildJavaAgentString();
        plugin.get("configuration")
                .make() // If not exits
                .get("useManifestOnlyJar")
                    .make()
                    .text("false").__
                .get("argLine")
                    .make()
                    .text(agentLine);
    }

    InputStream getScriptInputStream() {
        try {
            return new FileInputStream(buildScript.toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                    "MavenProjectPreparer: Could not read build script " + buildScript);
        }
    }

    OutputStream getScriptOutputStream() {
        try {
            return new FileOutputStream(buildScript.toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                    "MavenProjectPreparer: Could not read build script " + buildScript);
        }
    }

}
