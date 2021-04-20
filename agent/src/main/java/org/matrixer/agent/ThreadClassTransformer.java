package org.matrixer.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.function.Consumer;

import org.objectweb.asm.*;


class ThreadClassTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        Consumer<String> foo = str -> InvocationLogger.newThread(str);
        System.getProperties().put("__$__Thread<init>__$__", foo);

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
                                    visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System",
                                            "getProperties", "()Ljava/util/Properties;", false);
                                    visitLdcInsn("__$__Thread<init>__$__");
                                    visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Properties",
                                            "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                                    visitTypeInsn(Opcodes.CHECKCAST, "java/util/function/Consumer");
                                    visitVarInsn(Opcodes.ALOAD, 0);
                                    visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Thread",
                                            "getName", "()Ljava/lang/String;", false);
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
