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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.function.Consumer;

import org.objectweb.asm.*;

public class ThreadClassTransformer implements ClassFileTransformer {

    private final static String propKey = "__$__Thread<init>__$__";

    public ThreadClassTransformer(Consumer<Thread> onThreadCreate) {
        System.getProperties().put(propKey, onThreadCreate);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {

        if (className.equals("java/lang/Thread") && classBeingRedefined == Thread.class) {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, 0);
            ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                        String signature, String[] exceptions) {
                    MethodVisitor mv =
                            super.visitMethod(access, name, descriptor, signature, exceptions);
                    if (name.equals("<init>") && descriptor.endsWith("Z)V")) {
                        mv = new MethodVisitor(Opcodes.ASM9, mv) {
                            @Override
                            public void visitInsn(int opcode) {
                                if (opcode == Opcodes.RETURN) {
                                    // Properties p = System.getProperties();
                                    visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System",
                                            "getProperties", "()Ljava/util/Properties;", false);

                                    // Object obj = p.get(...)
                                    visitLdcInsn(propKey);
                                    visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Properties",
                                            "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);

                                    // Consumer consumer = (Consumer) obj;
                                    visitTypeInsn(Opcodes.CHECKCAST, "java/util/function/Consumer");

                                    // consumer.accept(this)
                                    visitVarInsn(Opcodes.ALOAD, 0);  // Loads "this" to stack
                                    visitMethodInsn(Opcodes.INVOKEINTERFACE,
                                            "java/util/function/Consumer", "accept",
                                            "(Ljava/lang/Object;)V", true);
                                }
                                super.visitInsn(opcode);
                            }

                            @Override
                            public void visitMaxs(int maxStack, int maxLocals) {
                                super.visitMaxs(Math.max(2, maxStack), maxLocals);
                            }
                        };
                    }
                    return mv;
                }
            };
            cr.accept(cv, 0);
            classfileBuffer = cw.toByteArray();
        }
        return classfileBuffer;
    }
}
