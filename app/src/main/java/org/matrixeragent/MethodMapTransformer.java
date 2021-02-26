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

    MethodMapTransformer(String className, ClassLoader classLoader, String outputPath) {
        super(className, classLoader);
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
        // String fname = "/tmp/matrixer-test/matrix-cov/results.txt";
        String fname = outputPath + File.separator + "/results.txt";
        StringBuilder endBlock = new StringBuilder();
        endBlock.append(
                "try {"
                        + "java.io.File results = new java.io.File(\"" + fname + "\");"
                        + "java.io.FileOutputStream fos = new java.io.FileOutputStream(results);"
                        + "java.io.BufferedOutputStream out = new java.io.BufferedOutputStream(fos);"
                        + "StackTraceElement[] elems = Thread.currentThread().getStackTrace();"
                        // First StackTraceElement is getStackTrace()
                        + "for (int i = 1; i < elems.length; i++) {"
                        + "   StackTraceElement elem = elems[i];"
                        + "   if (elem.getClassLoaderName() == null) {"
                        + "       elem = elems[i-1];"
                        + "       String caller = elem.getClassName() + \":\" + elem.getMethodName();"
                        + "       String towrite = \"" + name + " <- \" + caller + \"\\n\";"
                        + "       System.out.println(towrite);"
                        + "       out.write(towrite.getBytes());"
                        + "       break;"
                        + "   }"
                        + "}"
                        + "out.close();"
                        + "fos.close();"
                        + "} catch(java.io.IOException e) {"
                        + "    System.err.println(\"Something went wrong! \" + e);"
                        + "}");
        method.insertBefore(endBlock.toString());
        return true;
    }
}
