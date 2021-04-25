package org.matrixer.agent.instrumentation;

import static org.matrixer.agent.MatrixerAgentUtils.isTestClass;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import org.objectweb.asm.*;
import org.objectweb.asm.util.*;

public class TestCaseTransformer implements ClassFileTransformer {

    private static final boolean debug = false; 
    private static final int VERSION = Opcodes.ASM9;
    private static final String fileSeparator = System.getProperty("file.separator");

    private final String targetName;

    public TestCaseTransformer(Class<?> target) {
        this.targetName = target.getName().replaceAll("\\.", fileSeparator);
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!className.equals(targetName)) {
            return null;
        }
        PrintWriter printWriter = new PrintWriter(System.out, true);
        Printer printer = new Textifier();

        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor parent = (debug) ? new TraceClassVisitor(cw, printer, printWriter) : cw;
        ClassVisitor cv = new TestCaseClassAdapter(VERSION, parent, className);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}
