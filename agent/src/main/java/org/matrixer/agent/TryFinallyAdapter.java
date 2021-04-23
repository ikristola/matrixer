package org.matrixer.agent;

import org.objectweb.asm.*;

// Requires COMPUTE_FRAMES option in ClassWriter
public class TryFinallyAdapter extends MethodVisitor implements Opcodes {

    private String methodName;
    private String desc;

    private int returnInsn;  // XRETURN 
    private int loadInsn;    // XLOAD
    private int storeInsn;   // XSTORE

    private Label tryLabel = new Label();
    private Label exitReturnLabel = new Label();
    private Label exitExceptionLabel = new Label();

    public TryFinallyAdapter(int version, int access, String name, String desc,
            MethodVisitor methodVisitor) {
        super(version, methodVisitor);
        this.methodName = name;
        this.desc = desc;
        setTypes();
    }

    void setTypes() {
        switch(Type.getReturnType(desc).getSort()) {
            case Type.BYTE:
                returnInsn = IRETURN;
                loadInsn = ILOAD;
                storeInsn = ISTORE;
            case Type.SHORT:
                returnInsn = IRETURN;
                loadInsn = ILOAD;
                storeInsn = ISTORE;
            case Type.CHAR:
                returnInsn = IRETURN;
                loadInsn = ILOAD;
                storeInsn = ISTORE;
            case Type.INT:
                returnInsn = IRETURN;
                loadInsn = ILOAD;
                storeInsn = ISTORE;
                break;
            case Type.LONG:
                returnInsn = LRETURN;
                loadInsn = LLOAD;
                storeInsn = LSTORE;
                break;
            case Type.FLOAT:
                returnInsn = FRETURN;
                loadInsn = FLOAD;
                storeInsn = FSTORE;
                break;
            case Type.DOUBLE:
                returnInsn = DRETURN;
                loadInsn = DLOAD;
                storeInsn = DSTORE;
                break;
            case Type.VOID:
                returnInsn = RETURN;
                loadInsn = NOP;
                storeInsn = NOP;
                break;
            case Type.ARRAY:  // falthrough
            case Type.OBJECT:
                returnInsn = ARETURN;
                loadInsn = ALOAD;
                storeInsn = ASTORE;
                break;
        }

    }

    @Override
    public void visitCode() {
        visitTryCatchBlock(tryLabel, exitReturnLabel, exitExceptionLabel, null);
        println("Entering " + methodName + " " + desc);
        visitLabel(tryLabel);
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        switch(opcode) {
        case IRETURN:  // fallthrough
        case LRETURN:  // fallthrough
        case FRETURN:  // fallthrough
        case DRETURN:  // fallthrough
        case ARETURN:  // fallthrough
        case RETURN:
            visitVarInsn(storeInsn, 3);
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
        println("Exiting by returning");
        visitVarInsn(loadInsn, 3);
        super.visitInsn(returnInsn); // Avoid looping if calling this.visitInsn()

        visitLabel(exitExceptionLabel);
        visitVarInsn(ASTORE, 3);
        println("Exiting with exception");
        visitVarInsn(ALOAD, 3);
        super.visitInsn(Opcodes.ATHROW);

        super.visitMaxs(0, 0); // Must be computed automatically with COMPUTE_FRAMES in ClassWriter
    }

    private void println(String line) {
        visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
        visitLdcInsn(line);
        visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println",
                "(Ljava/lang/String;)V", false);
    }

}
