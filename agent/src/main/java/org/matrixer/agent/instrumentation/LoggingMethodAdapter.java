package org.matrixer.agent.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LoggingMethodAdapter extends TryFinallyMethodWrapper {

    public LoggingMethodAdapter(int api, MethodVisitor mv, String methodName) {
        super(api, mv, methodName);
    }

    @Override
    protected void onMethodEnter() {
        mv.visitLdcInsn(methodName);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/matrixer/agent/InvocationLogger", "pushMethod",
                "(Ljava/lang/String;)V", false);
    }

    @Override
    protected void onMethodExit() {
        mv.visitLdcInsn(methodName);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/matrixer/agent/InvocationLogger", "popMethod",
                "(Ljava/lang/String;)V", false);
    }
}
