package org.matrixer.agent.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

class TestCaseClassAdapter extends ClassVisitor {
    String className;

    public TestCaseClassAdapter(int version, ClassVisitor cv, String className) {
        super(version, cv);
        this.className = className.replace('/', '.');
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String sign,
            String[] exceptions) {

        // Does not work for (static/non-static) constructors
        if (name.equals("<init>") || name.equals("<clinit>")) {
            return super.visitMethod(access, name, desc, sign, exceptions);
        }
        MethodVisitor mv = super.visitMethod(access, name, desc, sign, exceptions);
        return new TestCaseMethodAdapter(api, mv, testCaseName(name));
    }

    private String testCaseName(String name) {
        return className + "." + name;
    }
}
