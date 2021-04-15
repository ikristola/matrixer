package org.matrixer.agent;

import java.io.IOException;
import javassist.*;

/**
 * Transformer that inserts code for printing out caller method into
 * target class methods
 */
public class MethodMapTransformer extends Transformer {

    /**
     * The package under test Will be used to match classes to instrument
     */
    final private String targetPackageName;

    /**
     * Creates a new transformer
     *
     * @param cls
     *            The class to transform
     * @param targetPackage
     *            The root package name for the target of the
     *            instrumentation
     * @param testerPackage
     *            The root package name for the testing package
     */
    MethodMapTransformer(Class<?> cls, String targetPackage, String testerPackage) {
        super(cls.getName(), cls.getClassLoader());
        this.targetPackageName = targetPackage;
    }

    /**
     * Transform a method so that it prints out the caller method when
     * called
     *
     * @param method
     *            The method to be instrumented
     * @return True if successful
     * @throws NotFoundException
     * @throws CannotCompileException
     * @throws IOException
     */
    public boolean instrument(CtMethod method) throws CannotCompileException, IOException {

        final var methodName = method.getLongName();
        if (!methodName.startsWith(targetPackageName)) {
            return false;
        }
        System.out.println("[MethodMapTransformer] Found method: " + methodName);
        String instrumentation = getCodeString(methodName);
        method.insertBefore(instrumentation);

        return true;
    }

    /**
     * Returns the java source code to inject each method with.
     *
     * @param methodName
     *            the filename that the method should write to.
     * @param calledMethodName
     *            the name of the method that will be instumented
     */
    private String getCodeString(String methodName) {
        StringBuilder builder = new StringBuilder();
        builder.append(InvocationLogger.class.getName())
                .append(".report(\"")
                .append(methodName)
                .append("\", Thread.currentThread().getStackTrace());");
        return builder.toString();
    }
}
