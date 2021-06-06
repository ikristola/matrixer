/**
 * Copyright 2021 Patrik Bogren, Isak Kristola
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.matrixer.agent.instrumentation;

import org.objectweb.asm.*;

/**
 * Wraps the original method in a try finally block.
 *
 * The wrapped body will behave like this
 *
 * <pre>
 * onMethodBegin()
 * try {
 *  // Original code
 * } finally {
 *  onMethodEnd();
 * }
 * </pre>
 */
public abstract class TryFinallyMethodWrapper extends MethodVisitor {

    protected String methodName;

    private final Label originalContentBegin = new Label();
    private final Label originalContentEnd = new Label();

    public TryFinallyMethodWrapper(int api, MethodVisitor methodVisitor, String methodName) {
        super(api, methodVisitor);
        this.methodName = methodName;
    }

    /*
     * Beginning of method body
     */
    @Override
    public void visitCode() {
        super.visitCode();
        onMethodEnter();

        // Beginning of try block
        visitLabel(originalContentBegin);
    }

    /*
     * Called on each instruction.
     */
    @Override
    public void visitInsn(int opcode) {
        // Insert the finally code before each return instruction
        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
            onMethodExit();
        }
        super.visitInsn(opcode);
    }

    /*
     * Called last in the method body
     *
     * Must specify the size of the operand stack and number of local
     * variables used in the method.
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // End of try block
        // Beginning of exception handler / finally block
        visitLabel(originalContentEnd);

        // Declares the instructions that should be covered by the exception
        // handler
        visitTryCatchBlock(originalContentBegin, originalContentEnd, originalContentEnd, null);
        // Restore previous frame
        // This code will only be called when exceptions are caught
        visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Throwable"});
        onMethodExit();
        // Rethrow exception
        super.visitInsn(Opcodes.ATHROW);

        super.visitMaxs(maxStack, maxLocals);
    }

    /*
     * Called after visitMaxs
     */
    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    /**
     * Called when the method body begins
     */
    protected abstract void onMethodEnter();

    /**
     * Called before each exit path from the method. Whether ny returning or
     * throwing an exception. Th
     */
    protected abstract void onMethodExit();

}
