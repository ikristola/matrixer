package org.matrixeragent;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * Abstract base class for transforming classes using the Instrument API and Javassist.
 * Concrete subclasses must implement the instrument method which specifies
 * how the classes are transformed.
 */
public abstract class Transformer implements ClassFileTransformer {

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
                System.out.println("[Transformer] Transforming class " + className);
                ClassPool pool = ClassPool.getDefault();
                CtClass cc = pool.get(targetClassName);
                for (var m : cc.getMethods()) {
                    if (instrument(m)) {
                        System.out.println("[Transformer] Instrumented " + m.getLongName());
                    }
                }
                byteCode = cc.toBytecode();
                cc.detach();
                return byteCode;
            } catch (CannotCompileException e) {
                System.err.println("[Transformer] Err Transformer.transform(): " + e.getReason());
            } catch (NotFoundException | IOException e) {
                System.err.println("[Transformer] Err Transformer.transform(): " + e);
            }
        }
        return null;
    }

    abstract boolean instrument(CtMethod method) throws NotFoundException, CannotCompileException, IOException;

}
