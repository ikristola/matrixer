package org.matrixer.agent;

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
            tryLoadClassesFromPath(classPathEntry, base);
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

    private void tryLoadClassesFromPath(String classPathEntry, File base) {
        try {
            loadClassesFromPath(classPathEntry, base);
        } catch (Exception ex) {
            // What to do here?
            System.out.println(ex.getMessage());
        }
    }

    private void loadClassesFromPath(String classPathEntry, File base) throws Exception {
        for (File file : base.listFiles()) {
            if (file.isDirectory()) {
                loadClassesFromPath(classPathEntry, file);
                continue;
            }
            String canonical = file.getCanonicalPath().toString();
            String name = canonical.substring(classPathEntry.length() + 1);
            String className = stripClassExtension(name.replaceAll("[\\\\/]", "."));
            System.out.println("Trying to load: " + className);
            if (name.endsWith(".class")) {
                try {
                    classes.add(Class.forName(className));
                } catch (Exception e) {
                    throw new RuntimeException("Could not find: " + className + "\n\t" + canonical);
                }
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
