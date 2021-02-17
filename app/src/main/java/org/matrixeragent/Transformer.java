package org.matrixeragent;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * Abstract base class for transforming classes using the Instrument API
 * and Javassist. Concrete subclasses must implement the instrument
 * method which specifies how the classes are transformed.
 */
public abstract class Transformer implements ClassFileTransformer {

    String targetClassName;
    ClassLoader targetClassLoader;

    Transformer(String className, ClassLoader classLoader) {
        this.targetClassName = className;
        this.targetClassLoader = classLoader;
    }

    /**
     * Transforms a class. This method is meant to be called by an java
     * agent in production code.
     * 
     * @return A bytearray containing the instrumented class
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> cls,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        byte[] byteCode = classfileBuffer;
        String finalTargetClassName = this.targetClassName.replaceAll("\\.", "/");

        if (!className.equals(finalTargetClassName)) {
            return byteCode;
        }
        if (loader.equals(targetClassLoader)) {
            return tryTransform(className);
        }
        return null;
    }

    private byte[] tryTransform(String className) {
        try {
            System.out.println("[Transformer] Transforming class " + className);
            return transform(className);
        } catch (CannotCompileException e) {
            System.err.println("[Transformer] Err Transformer.transform(): " + e.getReason());
        } catch (NotFoundException | IOException e) {
            System.err.println("[Transformer] Err Transformer.transform(): " + e);
        }
        return null;
    }

    private byte[] transform(String className)
            throws IOException, NotFoundException, CannotCompileException {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.get(targetClassName);
        instrument(cc);
        byte[] byteCode = cc.toBytecode();
        cc.detach();
        return byteCode;
    }

    private void instrument(CtClass cls)
            throws IOException, NotFoundException, CannotCompileException {
        for (var m : cls.getMethods()) {
            if (instrument(m)) {
                System.out.println("[Transformer] Instrumented " + m.getLongName());
            }
        }
    }

    /**
     * Instruments a method. Must be implemented by concrete subclasses.
     * 
     * @param method The method to be instrumented
     * @return True if successful
     * @throws NotFoundException
     * @throws CannotCompileException
     * @throws IOException
     */
    abstract boolean instrument(CtMethod method)
            throws NotFoundException, CannotCompileException, IOException;

}
