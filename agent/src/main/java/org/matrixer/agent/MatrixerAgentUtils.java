package org.matrixer.agent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Utility functions used by the agent
 */
public class MatrixerAgentUtils {

    /**
     * Get a list of all classes in package
     * 
     * @param packageName
     *            Target package
     * @return List of classes
     */
    public static final List<Class<?>> getClassesInPackage(String packageName) {
        var classLoader = new PackageClassLoader(packageName);
        classLoader.loadClasses();
        return classLoader.getClasses();
    }

    /**
     * Determine if a class contains Junit unit tests
     *
     * @param cls
     *            The class to examine
     * @return True if class contains test
     */
    public static final boolean isTestClass(Class<?> cls) {
        Method[] m = cls.getDeclaredMethods();
        for (Method method : m) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.toString().matches("^@org.(junit|testng).*")) {
                    return true;
                }
            }
        }
        Class<?> enclosingClass = cls.getEnclosingClass();
        if (enclosingClass != null && isTestClass(enclosingClass)) {
            return true;
        }
        Class<?> declaringClass = cls.getDeclaringClass();
        if (declaringClass != null && isTestClass(declaringClass)) {
            return true;
        }
        return false;
    }
}
