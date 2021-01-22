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
    public byte[] transform(ClassLoader loader, String className, Class<?> cls,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {

        String finalTargetClassName =
                this.targetClassName.replaceAll("\\.", "/");

        if (!className.equals(finalTargetClassName) ||
                !loader.equals(targetClassLoader)) {
            return classfileBuffer;
        }

        System.out.println("[Agent] Transforming class " + className);
        return instrumentClass();
    }

    private byte[] instrumentClass() {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.get(targetClassName);
            instrumentMethods(cc);
            byte[] byteCode = cc.toBytecode();
            cc.detach();
            return byteCode;
        } catch (CannotCompileException e) {
            System.err.println("[Agent] Err Transformer.transform(): " + e.getReason());
        } catch (NotFoundException | IOException e) {
            System.err.println("[Agent] Err Transformer.transform(): " + e);
        }
        return null;
    }

    private void instrumentMethods(CtClass cls)
            throws NotFoundException, CannotCompileException, IOException {
        for (var m : cls.getMethods()) {
            final var methodName = m.getLongName();
            // Skip methods inherited from Object
            if (methodName.startsWith("java.lang.Object")) {
                continue;
            }
            System.out.println("[Agent] Found method: " + methodName);
            if (instrument(m)) {
                System.out.println("[Agent] Instrumented " + methodName);
            }
        }
    }

    private boolean instrument(CtMethod method) 
            throws CannotCompileException, IOException {

        final var name = method.getLongName();
        StringBuilder endBlock = new StringBuilder();
        endBlock.append(
                "StackTraceElement[] elems = Thread.currentThread().getStackTrace();"
                        // First StackTraceElement is getStackTrace()
                        + "for (int i = 1; i < elems.length; i++) {"
                        + "   StackTraceElement elem = elems[i];"
                        + "   if (elem.getClassLoaderName() == null) {"
                        + "       elem = elems[i-1];"
                        + "       String caller = elem.getClassName() + \":\" + elem.getMethodName();"
                        + "       System.out.println(\"Looks like " + name
                        + " was called by test \" + caller);"
                        + "       break;"
                        + "   }"
                        + "};");
        method.insertAfter(endBlock.toString());
        return true;
    }
}
