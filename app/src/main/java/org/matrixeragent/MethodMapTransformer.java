package org.matrixeragent;

import java.io.IOException;
import java.io.File;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.CtMethod;

/**
 * Transformer that inserts code for printing out caller method into
 * target class methods
 */
public class MethodMapTransformer extends Transformer {

    String outputPath;
    String className;

    MethodMapTransformer(String className, ClassLoader classLoader, String outputPath) {
        super(className, classLoader);
        this.className = className;
        this.outputPath = outputPath;
    }

    /**
     * Transform a method so that it prints out the caller method when
     * called
     * 
     * @param method The method to be instrumented
     * @return True if successful
     * @throws NotFoundException
     * @throws CannotCompileException
     * @throws IOException
     */
    public boolean instrument(CtMethod method)
            throws NotFoundException, CannotCompileException, IOException {

        final var name = method.getLongName();
        if (name.startsWith("java.lang.Object")) {
            return false;
        }

        System.out.println("[MethodMapTransformer] Found method: " + name);
        String fname = outputPath + File.separator + className + ".txt";
        String endBlock = String.format(
                "try {"
                        + "java.io.File results = new java.io.File(\"%1$s\");"
                        + "java.io.FileOutputStream fos = new java.io.FileOutputStream(results, true);"
                        + "java.io.BufferedOutputStream out = new java.io.BufferedOutputStream(fos);"
                        + "StackTraceElement[] elems = Thread.currentThread().getStackTrace();"
                        // First StackTraceElement is getStackTrace()
                        + "for (int i = 1; i < elems.length; i++) {"
                        + "   StackTraceElement elem = elems[i];"
                        + "   if (elem.getClassLoaderName() == null) {"
                        + "       elem = elems[i-1];"
                        + "       String caller = elem.getClassName() + \":\" + elem.getMethodName();"
                        + "       String towrite = \"%2$s <- \" + caller + \"\\n\";"
                        + "       System.out.print(towrite);"
                        + "       out.write(towrite.getBytes());"
                        + "       break;"
                        + "   }"
                        + "}"
                        + "out.close();"
                        + "} catch(java.io.IOException e) {"
                        + "    System.err.println(\"Something went wrong! \" + e);"
                        + "}", fname, name);
        method.insertBefore(endBlock);
        return true;
    }
}
