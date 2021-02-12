package org.matrixeragent;

import java.io.IOException;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.CtMethod;

/**
 * Transformer that inserts code for printing out caller method into target class methods
 */
public class MethodMapTransformer extends Transformer {

    MethodMapTransformer(String className, ClassLoader classLoader) {
        super(className, classLoader);
    }

    public boolean instrument(CtMethod method)
            throws NotFoundException, CannotCompileException, IOException {

        final var name = method.getLongName();
        if (name.startsWith("java.lang.Object")) {
            return false;
        }

        System.out.println("[MethodMapTransformer] Found method: " + name);
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
