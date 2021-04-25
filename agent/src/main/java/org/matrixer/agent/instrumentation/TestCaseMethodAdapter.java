package org.matrixer.agent.instrumentation;

import org.objectweb.asm.*;

public class TestCaseMethodAdapter extends TryFinallyMethodWrapper {

    private boolean isTestCase = false;

    public TestCaseMethodAdapter(int api, MethodVisitor mv, String methodName) {
        super(api, mv, methodName);
    }

    @Override 
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.endsWith("Test;")) {
            isTestCase = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    protected void onMethodEnter() {
        if (isTestCase) {
            logBeginTestCase();
        } 
    }

    private void logBeginTestCase() {
        mv.visitLdcInsn(methodName);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/matrixer/agent/InvocationLogger", "beginTestCase",
                "(Ljava/lang/String;)V", false);
    }

    @Override
    protected void onMethodExit() {
        if (isTestCase) {
            logEndTestCase();
        }
    }

    private void logEndTestCase() {
        mv.visitLdcInsn(methodName);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/matrixer/agent/InvocationLogger", "endTestCase",
                "(Ljava/lang/String;)V", false);
    }
}
