package org.matrixer.agent;

import java.io.IOException;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class Transformer implements ClassFileTransformer {

        String targetClassName;
        ClassLoader targetClassLoader;

    Transformer(String className, ClassLoader classLoader) {
        this.targetClassName = className;
        this.targetClassLoader = classLoader;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> cls, ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {
        byte[] byteCode = classfileBuffer;
        String finalTargetClassName = this.targetClassName.replaceAll("\\.", "/");

        if (!className.equals(finalTargetClassName)) {
            return byteCode;
        }

        if (className.equals(finalTargetClassName) && loader.equals(targetClassLoader)) {
            System.out.println("[Agent] Transforming class " + className);
        }

        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.get(targetClassName);
            for (var m : cc.getMethods()) {
                if (instrument(m)) {
                    System.out.println("Instrumented " + m.getLongName());
                }
            }
            byteCode = cc.toBytecode();
            cc.detach();
            return byteCode;
        } catch (NotFoundException | CannotCompileException | IOException e) {
            return null;
        }
    }

    private boolean instrument(CtMethod method) throws NotFoundException, CannotCompileException, IOException {
            final var name = method.getLongName();
            if (name.startsWith("java.lang.Object")) {
                return false;
            }

            System.out.println("Found method: " + name);


            StringBuilder endBlock = new StringBuilder();
            endBlock.append("System.out.println(getClass().getName() + \": Oh good, I have been hijacked!!!\");");
            method.insertAfter(endBlock.toString());
            return true;
    }
}
