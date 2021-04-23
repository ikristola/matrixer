package org.matrixer.agent;

import org.objectweb.asm.*;

// Requires COMPUTE_FRAMES option in ClassWriter
public class TryFinallyAdapter extends MethodVisitor implements Opcodes {

    private String methodName;
    private String className;
    private String desc;

    private int returnInsn; // XRETURN

    private Label tryLabel = new Label();
    private Label exitReturnLabel = new Label();
    private Label exitExceptionLabel = new Label();

    public TryFinallyAdapter(int version, int access, String className, String methodName,
            String methodDesc, MethodVisitor methodVisitor) {
        super(version, methodVisitor);
        this.methodName = methodName;
        this.className = className.replaceAll(System.getProperty("file.separator"), ".");
        this.desc = methodDesc;
        returnInsn = getReturnType(desc);
    }

    int getReturnType(String desc) {
        switch (Type.getReturnType(desc).getSort()) {
            case Type.BYTE: // fallthrough
            case Type.SHORT: // fallthrough
            case Type.CHAR: // fallthrough
            case Type.INT:
                return IRETURN;
            case Type.LONG:
                return LRETURN;
            case Type.FLOAT:
                return FRETURN;
            case Type.DOUBLE:
                return DRETURN;
            case Type.VOID:
                return RETURN;
            case Type.ARRAY: // falthrough
            case Type.OBJECT:
                return ARETURN;
            default: 
                throw new IllegalArgumentException("Unknown return type: " + desc);
        }
    }

    @Override
    public void visitCode() {
        visitTryCatchBlock(tryLabel, exitReturnLabel, exitExceptionLabel, null);
        println("Entering " + qMethodName());
        visitLabel(tryLabel);
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        switch (opcode) {
            case IRETURN: // fallthrough
            case LRETURN: // fallthrough
            case FRETURN: // fallthrough
            case DRETURN: // fallthrough
            case ARETURN: // fallthrough
            case RETURN:
                visitJumpInsn(GOTO, exitReturnLabel);
                break;
            case ATHROW:
                visitJumpInsn(GOTO, exitExceptionLabel);
                break;
            default:
                super.visitInsn(opcode);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        visitLabel(exitReturnLabel);
        println("Exiting " + qMethodName() + " by returning");
        super.visitInsn(returnInsn); // Avoid looping if calling this.visitInsn()

        visitLabel(exitExceptionLabel);
        println("Exiting " + qMethodName() + " with exception");
        super.visitInsn(Opcodes.ATHROW);

        super.visitMaxs(0, 0); // Must be computed automatically with COMPUTE_FRAMES in ClassWriter
    }

    private void println(String line) {
        visitLdcInsn(line);
        visitMethodInsn(Opcodes.INVOKESTATIC, "org/matrixer/agent/InvocationLogger", "newThread",
                "(Ljava/lang/String;)V", false);
    }

    String qMethodName() {
        return className + "." + methodName + " " + desc;
    }

}
