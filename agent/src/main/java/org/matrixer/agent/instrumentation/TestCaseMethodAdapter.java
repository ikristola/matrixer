package org.matrixer.agent.instrumentation;

import org.objectweb.asm.*;

public class TestCaseMethodAdapter extends TryFinallyMethodWrapper {

    private boolean isTestCase = false;

    public TestCaseMethodAdapter(int api, MethodVisitor mv, String methodName) {
        super(api, mv, methodName);
    }

    /*
     * Annotations are visited before the method body so it can be used to detect
     * Test annotations.
     */
    @Override 
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.endsWith("/Test;")) {
            isTestCase = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitCode() {
        if (isTestCase) {
            super.visitCode();
        } else {
            // Short out instrumentation
            mv.visitCode();
        }
    }

    @Override
    protected void onMethodEnter() {
        if (isTestCase) {
            logBeginTestCase();
        } 
    }

    @Override
    public void visitInsn(int opcode) {
        if (isTestCase) {
            super.visitInsn(opcode);
        } else {
            // Short out instrumentation
            mv.visitInsn(opcode);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        if (isTestCase) {
            super.visitMaxs(maxStack, maxLocals);
        } else {
            // Short out instrumentation
            mv.visitMaxs(maxStack, maxLocals);
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
