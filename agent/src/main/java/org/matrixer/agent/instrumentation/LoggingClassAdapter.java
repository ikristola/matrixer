/**
 * Copyright 2021 Patrik Bogren, Isak Kristola
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.matrixer.agent.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class LoggingClassAdapter extends ClassVisitor {

    String className;
    private static final String pathSeparator = System.getProperty("file.separator");

    public LoggingClassAdapter(int version, ClassVisitor cv, String className) {
        super(version, cv);
        this.className = className.replaceAll(pathSeparator, ".");
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String sign,
            String[] exceptions) {

        // Does not work for (static/non-static) constructors
        if (name.equals("<init>") || name.equals("<clinit>")) {
            return super.visitMethod(access, name, desc, sign, exceptions);
        }
        MethodVisitor mv = super.visitMethod(access, name, desc, sign, exceptions);
        return new LoggingMethodAdapter(api, mv, qualifiedMethodName(name, desc));
    }

    private String qualifiedMethodName(String name, String desc) {
        return className + "." + name + desc;
    }
}
