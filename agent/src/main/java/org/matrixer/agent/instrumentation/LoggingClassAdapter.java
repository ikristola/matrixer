package org.matrixer.agent.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class LoggingClassAdapter extends ClassVisitor {

    String className;
    private static final String pathSeparator = System.getProperty("file.separator");

    public LoggingClassAdapter(int version, ClassVisitor cv, String className) {
        super(version, cv);
        this.className = className.replaceAll(pathSeparator, ".");
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String sign,
            String[] exceptions) {

        // Does not work for (static/non-static) constructors
        if (name.equals("<init>") || name.equals("<clinit>")) {
            return super.visitMethod(access, name, desc, sign, exceptions);
        }
        MethodVisitor mv = super.visitMethod(access, name, desc, sign, exceptions);
        return new LoggingMethodAdapter(api, mv, qualifiedMethodName(name, desc));
    }

    private String qualifiedMethodName(String name, String desc) {
        return className + "." + name + desc;
    }
}
