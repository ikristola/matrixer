package org.matrixer.agent.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.matrixer.agent.MatrixerAgent;
import org.matrixer.core.runtime.AgentOptions;
import org.matrixer.core.runtime.Logger;
import org.objectweb.asm.*;


public class CallLoggingTransformer implements ClassFileTransformer {

    private static final String AGENT_PREFIX;
    static {
        final String name = MatrixerAgent.class.getName();
        AGENT_PREFIX = toVMName(name.substring(0, name.lastIndexOf('.')));
    }

    private static final int VERSION = Opcodes.ASM9;
    private static boolean debug = false;

    private String pkg;
    private Instrumenter instrumenter;
    private Logger logger;

    public CallLoggingTransformer(AgentOptions options, Logger logger) {
        this(options.getTargetPackage(), logger);
        debug = options.getDebug();
    }
    public CallLoggingTransformer(String pkg, Logger logger) {
        this.pkg = pkg;
        this.instrumenter = new Instrumenter(debug);
        this.logger = logger;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        if (classBeingRedefined != null) {
            // Already loaded class
            return null;
        }

        URL location = getLocation(protectionDomain);
        if (!shouldTransform(location, loader, className)) {
            return null;
        }
        if (isTestClass(className, location)) {
            log("Instrumenting test   " + className);
            return instrumenter.instrumentTestClass(VERSION, className, classfileBuffer);
        }
        log("Instrumenting target " + className);
        return instrumenter.instrumentTargetClass(VERSION, className, classfileBuffer);
    }

    boolean isTestClass(String className, URL location) {
        // Maven surefire places test classes in target/test-classes and target classes in
        // target/classes. Gradle preserves the hierarchy in src/
        return location.getFile().matches(".*/(test|test-classes)/.*");
    }

    boolean shouldTransform(URL location, final ClassLoader loader, String classname) {
        if (loader == null) {
            // Boot strap class
            return false;
        }
        if (location == null) {
            // Not sure what this means but we need the location
            // do distinguish test cases
            return false;
        }
        return !classname.startsWith(AGENT_PREFIX) && classname.startsWith(toVMName(pkg));
    }

    URL getLocation(ProtectionDomain protectionDomain) {
        if (protectionDomain == null) {
            return null;
        }
        CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            return null;
        }
        return codeSource.getLocation();
    }

    // java.lang.String -> java/lang/String
    private static String toVMName(String classname) {
        return classname.replace('.', '/');
    }

    // java/lang/String -> java.lang.String
    private static String toPkgName(String classname) {
        return classname.replace('/', '.');
    }

    private void log(String msg) {
        if (debug){
            logger.log("Transformer: " + msg);
        }
    }

    private void logError(String msg) {
        if (debug){
            logger.logError("Transformer: " + msg);
        }
    }
}
