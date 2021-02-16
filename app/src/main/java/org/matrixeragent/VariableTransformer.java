package org.matrixeragent;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;

/**
 * Transformer that takes a string parameter and inserts it into target class methods
 */
public class VariableTransformer extends Transformer {

    private final String insertString;

    VariableTransformer(String className, ClassLoader classLoader, String insertString) {
        super(className, classLoader);
        this.insertString = insertString;
    }

    /**
     * Inject the insertString in target method
     * @param method    The method to be instrumented
     * @return          True if successful
     * @throws NotFoundException
     * @throws CannotCompileException
     * @throws IOException
     */
    @Override
    boolean instrument(CtMethod method) throws NotFoundException, CannotCompileException, IOException {

        final var name = method.getLongName();
        if (name.startsWith("java.lang.Object")) {
            return false;
        }

        System.out.println("[VariableMapTransformer] Found method: " + name);
        method.insertAfter(insertString);
        return true;
    }
}
