package org.matrixer.agent.instrumentation;

import java.util.ArrayList;

import org.objectweb.asm.*;

public class CallLoggingClassAdapter extends ClassVisitor {

    private String className;

    ArrayList<OldMethod> methodsToAdd = new ArrayList<>();

    CallLoggingClassAdapter(int version, ClassVisitor cv, String className) {
        super(version, cv);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String sign,
            String[] exceptions) {

        // Skrip static and non-static constructors
        if (name.equals("<init>") || name.equals("<clinit>")) {
            return super.visitMethod(access, name, desc, sign, exceptions);
        }
        methodsToAdd.add(new OldMethod(access, name, desc, sign, exceptions));
        name = name + "$instrumented";
        MethodVisitor mv = super.visitMethod(access, name, desc, sign, exceptions);

        System.out.println("Instrumenting " + className + ":" + name + " " + desc);
        return new TryFinallyAdapter(api, access, className, name, desc, mv);
    }

    @Override
    public void visitEnd() {
        // Since we create the method to wrap an existing method:
        // the method is never
        for (var m : methodsToAdd) {
            MethodVisitor mv = super.visitMethod(m.access, m.name, m.desc, m.sign, m.exceptions);
            mv.visitCode();
            if ((m.access & Opcodes.ACC_PRIVATE) < 0) {
                System.out.println("Method is private");
            }
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, m.name + "$instrumented", m.desc, false);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        super.visitEnd();
    }

    private void println(MethodVisitor mv, String line) {
        mv.visitLdcInsn(line);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/matrixer/agent/InvocationLogger", "newThread",
                "(Ljava/lang/String;)V", false);
    }


    class OldMethod {
        int access;
        String name;
        String desc;
        String sign;
        String[] exceptions;

        public OldMethod(int access, String name, String desc, String sign, String[] exceptions) {
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.sign = sign;
            this.exceptions = exceptions;
        }
    }
}
