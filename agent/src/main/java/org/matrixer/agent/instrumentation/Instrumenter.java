package org.matrixer.agent.instrumentation;

import java.io.PrintWriter;

import org.objectweb.asm.*;
import org.objectweb.asm.util.*;

public class Instrumenter {

    private final boolean debug;

    public Instrumenter(boolean debug) {
        this.debug = debug;
    }

    public Instrumenter() {
        this.debug = false;
    }

    public byte[] instrumentTestClass(int VERSION, String className, byte[] classfileBuffer) {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor parent = getParentClassVisitor(cw);
        ClassVisitor cv = new TestCaseClassAdapter(VERSION, parent, className);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    public byte[] instrumentTargetClass(int VERSION, String className, byte[] classfileBuffer) {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor parent = getParentClassVisitor(cw);
        ClassVisitor cv = new LoggingClassAdapter(VERSION, parent, className);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    ClassVisitor getParentClassVisitor(ClassWriter cw) {
        if (debug) {
            PrintWriter printWriter = new PrintWriter(System.out, true);
            Printer printer = new Textifier();
            return new TraceClassVisitor(cw, printer, printWriter);
        }
        return cw;
    }

}
