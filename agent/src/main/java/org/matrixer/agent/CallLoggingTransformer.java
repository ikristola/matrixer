package org.matrixer.agent;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import org.objectweb.asm.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;


public class CallLoggingTransformer implements ClassFileTransformer {

    private static final int VERSION = Opcodes.ASM9;
    private static final String fileSeparator = System.getProperty("file.separator");

    String targetName;

    public CallLoggingTransformer(Class<?> target) {
        targetName = target.getName().replaceAll("\\.", fileSeparator);
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
        ClassVisitor cv = new ClassVisitor(VERSION, new TraceClassVisitor(cw, printer, printWriter)) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String sign, String[] exceptions) {
                MethodVisitor mv =
                        super.visitMethod(access, name, desc, sign, exceptions);

                // Skrip static and non-static constructors
                if (name.equals("<init>") || name.equals("<clinit>")) {
                    return mv;
                }
                System.out.println("Instrumenting " + className + ":" + name + " " + desc);
                return new TryFinallyAdapter(VERSION, access, className, name, desc, mv);
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}
