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

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.matrixer.core.runtime.PrintLogger;
import org.matrixer.core.runtime.MethodCall;

class CallLoggingTransformerTest {

    static PrintLogger logger = new PrintLogger(System.out);

    ClassLoader loader;
    ProtectionDomain protectionDomain;


    @BeforeEach
    void setup() {
        loader = getClass().getClassLoader();
        protectionDomain = getClass().getProtectionDomain();
    }

    @Test
    void should_not_transform_if_location_is_null() {
        CallLoggingTransformer t = new CallLoggingTransformer("org.matrixer-test", logger);
        URL location = null;
        String name = "org/matrixer-test/SomeClass";
        assertFalse(
                t.shouldTransform(location, loader, name));
    }

    @Test
    void should_not_transform_if_loader_is_null() {
        CallLoggingTransformer t = new CallLoggingTransformer("org.matrixer-test", logger);
        URL location = asURL("file:/tmp");
        String name = "org/matrixer-test/SomeClass";
        assertFalse(
                t.shouldTransform(location, null, name));
    }

    @Test
    void should_not_transform_classes_in_agent_package() {
        CallLoggingTransformer t = new CallLoggingTransformer("org.matrixer-test", logger);
        URL location = asURL("file:/tmp/matrixer-test");
        String name = "org/matrixer/Sample";
        assertFalse(
                t.shouldTransform(location, loader, name));
    }

    @Test
    void should_not_transform_classes_in_other_package() {
        CallLoggingTransformer t = new CallLoggingTransformer("org.matrixer-test", logger);
        URL location = asURL("file:/tmp/matrixer-test");
        String name = "org/matrixer/Sample";
        assertFalse(
                t.shouldTransform(location, loader, name));
    }

    @Test
    void should_transform_class_in_package() {
        CallLoggingTransformer t = new CallLoggingTransformer("org.matrixer-test", logger);
        URL location = asURL("file:/tmp/matrixer-test");
        String name = "org/matrixer-test/SomeClass";
        assertTrue(
                t.shouldTransform(location, loader, name));
    }

    @Test
    void does_not_transform_boot_strap_classes() {
        CallLoggingTransformer t = new CallLoggingTransformer("org.matrixer", logger);
        assertNull(
                t.transform(null, "org/matrixer/Sample", null, protectionDomain, new byte[] {0}));
    }

    @Test
    void does_not_retransform() throws IOException {
        CallLoggingTransformer t = new CallLoggingTransformer("org.matrixer", logger);
        final Class<?> cls = MethodCall.class;
        assertNull(
                t.transform(loader, cls.getName(), cls, protectionDomain, getClassBytes(cls)));
    }

    @Test
    void class_in_test_directory_is_test_class() {
        CallLoggingTransformer t = new CallLoggingTransformer("org.matrixer", logger);
        assertTrue(t.isTestClass("", asURL("file:/tmp/project/src/test/java/package")));
    }

    @Test
    void class_in_non_test_directory_is_not_test_class() {
        CallLoggingTransformer t = new CallLoggingTransformer("org.matrixer", logger);
        assertFalse(t.isTestClass("", asURL("file:/tmp/project/src/main/java/package")));
    }

    private static byte[] getClassBytes(Class<?> clazz) throws IOException {
        final String resource = "/" + clazz.getName().replace('.', '/')
                + ".class";
        final InputStream in = clazz.getResourceAsStream(resource);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[0x100];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        in.close();
        return out.toByteArray();
    }

    ClassLoader getAppClassLoader() {
        return CallLoggingTransformerTest.class.getClassLoader();
    }

    URL asURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AssertionError("Test error: " + e.getMessage());
        }
    }

}
