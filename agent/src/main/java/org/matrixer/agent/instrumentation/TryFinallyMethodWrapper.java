package org.matrixer.agent.instrumentation;

import org.objectweb.asm.*;

public abstract class TryFinallyMethodWrapper extends MethodVisitor {

    protected String methodName;

    private final Label originalContentBegin = new Label();
    private final Label originalContentEnd = new Label();

    public TryFinallyMethodWrapper(int api, MethodVisitor methodVisitor, String methodName) {
        super(api, methodVisitor);
        this.methodName = methodName;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        onMethodEnter();
        visitLabel(originalContentBegin);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
            onMethodExit();
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        visitLabel(originalContentEnd);
        visitTryCatchBlock(originalContentBegin, originalContentEnd, originalContentEnd, null);
        visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Throwable"});
        onMethodExit();
        super.visitInsn(Opcodes.ATHROW);

        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    // Override this in subclasses
    protected abstract void onMethodEnter();

    // Override this in subclasses
    protected abstract void onMethodExit();

}
