package org.matrixeragent;

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
        byte[] byteCode = classfileBuffer;
        String finalTargetClassName =
                this.targetClassName.replaceAll("\\.", "/");

        if (!className.equals(finalTargetClassName)) {
            return byteCode;
        }

        if (loader.equals(targetClassLoader)) {
            try {
                System.out.println("[Agent] Transforming class " + className);
                ClassPool pool = ClassPool.getDefault();
                CtClass cc = pool.get(targetClassName);
                for (var m : cc.getMethods()) {
                    if (instrument(m)) {
                        System.out.println("[Agent] Instrumented " + m.getLongName());
                    }
                }
                byteCode = cc.toBytecode();
                cc.detach();
                return byteCode;
            } catch (CannotCompileException e) {
                System.err.println("[Agent] Err Transformer.transform(): " + e.getReason());
            } catch (NotFoundException | IOException e) {
                System.err.println("[Agent] Err Transformer.transform(): " + e);
            }
        }
        return null;
    }

    private boolean instrument(CtMethod method) 
            throws NotFoundException, CannotCompileException, IOException {

        final var name = method.getLongName();
        if (name.startsWith("java.lang.Object")) {
            return false;
        }

        System.out.println("[Agent] Found method: " + name);
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
