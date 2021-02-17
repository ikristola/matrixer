package org.matrixeragent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

class PackageClassLoader {
    String packageName;
    String path;
    List<Class<?>> classes;

    PackageClassLoader(String packageName) {
        this.packageName = packageName;
        this.path = packageName.replaceAll("\\.", File.separator);
    }

    public void loadClasses() {
        classes = new ArrayList<>();
        for (String classPathEntry : getClassPathEntries()) {
            loadClasses(classPathEntry);
        }
    }

    private String[] getClassPathEntries() {
        String sep = System.getProperty("path.separator");
        String classPath = "java.class.path";
        return System.getProperty(classPath).split(sep);
    }

    private void loadClasses(String classPathEntry) {
        if (classPathEntry.endsWith(".jar")) {
            File jar = new File(classPathEntry);
            tryLoadClassesFromJar(jar);
        } else {
            File base = new File(classPathEntry + File.separatorChar + path);
            tryLoadClassesFromPath(base);
        }
    }

    private void tryLoadClassesFromJar(File jar) {
        try (JarInputStream is = new JarInputStream(new FileInputStream(jar))) {
            loadClassesFromJar(jar, is);
        } catch (Exception ex) {
            // Silence is gold
            // What to do here?
        }
    }

    private void loadClassesFromJar(File jar, JarInputStream is)
            throws IOException, ClassNotFoundException {
        for (JarEntry entry; (entry = is.getNextJarEntry()) != null;) {
            String name = entry.getName();
            if (name.endsWith(".class") && name.contains(path)) {
                String classPath = stripClassExtension(name);
                classPath = classPath.replaceAll("[\\|/]", ".");
                classes.add(Class.forName(classPath));
            }
        }
    }

    private void tryLoadClassesFromPath(File base) {
        try {
            loadClassesFromPath(base);
        } catch (Exception ex) {
            // What to do here?
        }
    }

    private void loadClassesFromPath(File base) throws ClassNotFoundException {
        for (File file : base.listFiles()) {
            String name = file.getName();
            if (name.endsWith(".class")) {
                name = stripClassExtension(name);
                classes.add(Class.forName(packageName + "." + name));
            }
        }
    }

    String stripClassExtension(String path) {
        return path.substring(0, path.length() - 6);
    }

    public List<Class<?>> getClasses() {
        if (classes == null) {
            return new ArrayList<>();
        }
        return classes;
    }

}
