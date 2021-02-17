package org.matrixeragent;

import java.io.File;
import java.io.FileInputStream;
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
            loadClassesFromJar(jar);
        } else {
            File base = new File(classPathEntry + File.separatorChar + path);
            loadClassesFromPath(base);
        }
    }

    private void loadClassesFromJar(File jar) {
        try (JarInputStream is = new JarInputStream(new FileInputStream(jar))) {
            for (JarEntry entry; (entry = is.getNextJarEntry()) != null;) {
                String name = entry.getName();
                if (name.endsWith(".class") && name.contains(path)) {
                    String classPath = stripClassExtension(name);
                    classPath = classPath.replaceAll("[\\|/]", ".");
                    classes.add(Class.forName(classPath));
                }
            }
        } catch (Exception ex) {
            // Silence is gold
            // What to do here?
        }
    }

    private void loadClassesFromPath(File base) {
        try {
            for (File file : base.listFiles()) {
                String name = file.getName();
                if (name.endsWith(".class")) {
                    name = stripClassExtension(name);
                    classes.add(Class.forName(packageName + "." + name));
                }
            }
        } catch (Exception ex) {
            // What to do here?
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
