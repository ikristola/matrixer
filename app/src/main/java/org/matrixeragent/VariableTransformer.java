package org.matrixeragent;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;

/**
 * Transformer that takes a string parameter and inserts it into target class methods
 */
public class VariableTransformer extends Transformer {

    private String insertString;

    VariableTransformer(String className, ClassLoader classLoader, String insertString) {
        super(className, classLoader);
        this.insertString = insertString;
    }

    @Override
    boolean instrument(CtMethod method) throws NotFoundException, CannotCompileException, IOException {

        final var name = method.getLongName();
        if (name.startsWith("java.lang.Object")) {
            return false;
        }

        System.out.println("[VariableMapTransformer] Found method: " + name);
        StringBuilder endBlock = new StringBuilder();
        endBlock.append(insertString);
        method.insertAfter(endBlock.toString());
        return true;
    }
}
