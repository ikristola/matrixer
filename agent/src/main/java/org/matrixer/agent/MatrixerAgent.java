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
package org.matrixer.agent;

import java.io.*;
import java.lang.instrument.*;
import java.nio.file.*;
import java.util.function.Consumer;

import org.matrixer.agent.instrumentation.CallLoggingTransformer;
import org.matrixer.agent.instrumentation.ThreadClassTransformer;
import org.matrixer.core.runtime.*;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * MatrixerAgent instruments target classes to record the the call stack
 * depth of their invocations.
 *
 * The agent arguments are specified as a colon separated list of key
 * value pairs on the form key=value:
 *
 * <pre>
 * debug    - a boolean value that determines if the agent should print debug information
 * destFile - The path to the file where the matrixer data should be stored
 * pkg      - The package name for the classes that should be instrumented
 * testPkg  - Deprecated
 * depth    - An integer value specifying the maximum call stack depth to record
 * </pre>
 */
public class MatrixerAgent {

    private static MatrixerAgent agent;

    /**
     * Run when the agent is started statically
     *
     * @param agentArgs
     *            Agent arguments
     * @param inst
     *            Instrumentation instance
     * @throws IOException
     */
    public static void premain(String agentArgs, Instrumentation inst) throws IOException {
        agent = new MatrixerAgent(agentArgs, inst, "statically");
        agent.startup();
    }

    /**
     * Run when the agent is started dynamically
     *
     * @param agentArgs
     *            Agent arguments
     * @param inst
     *            Instrumentation instance
     * @throws IOException
     */
    public static void agentmain(String agentArgs, Instrumentation inst) throws IOException {
        agent = new MatrixerAgent(agentArgs, inst, "dynamically");
        agent.startup();
    }

    private Logger logger;

    final private Instrumentation inst;

    final AgentOptions options;

    private MatrixerAgent(String agentArgs, Instrumentation inst, String type) throws IOException {
        this.inst = inst;
        options = new AgentOptions(agentArgs);
        Path destDir = Path.of(options.getDestFilename()).getParent();
        Files.createDirectories(destDir);
        setupLog();
        log("started " + type + ":\n\tArgs: " + agentArgs);
        log(String.format("Output directory: %s\ntarget: %s\ntest: %s",
                options.getDestFilename(), options.getTargetPackage(),
                options.getTestPackage()));
    }

    private void setupLog() throws IOException {
        if (options.getDebug()) {
            logger = makePrintLogger();
        } else {
            logger = new NoopLogger();
        }
    }

    Logger makePrintLogger() throws IOException {
        Path destFile = Path.of(options.getDestFilename());
        Path logFile = destFile.resolveSibling("matrixer-agent-log.txt");
        var out = Files.newOutputStream(logFile, CREATE, APPEND);
        return new PrintLogger(new PrintStream(out), "[Matrixer]");
    }

    private void startup() {
        try {
            tryStartup();
        } catch (Throwable e) {
            logger.logException(e);
        }
    }

    private void tryStartup() throws IOException, UnmodifiableClassException {
        Path destFile = Path.of(options.getDestFilename());
        SynchronizedWriter writer = makeWriter(destFile);
        StackRecorder recorder = new StackRecorderImpl(writer, logger, options);
        InvocationLogger.init(recorder, logger);
        inst.addTransformer(new CallLoggingTransformer(options, logger));
        transformThreadClass(InvocationLogger::newThread);
    }

    SynchronizedWriter makeWriter(Path file) throws IOException {
        return new SynchronizedWriter(Files.newBufferedWriter(file, CREATE, APPEND));
    }

    /**
     * The call logging transformer needs to know when new threads are
     * created and the parent thread.
     *
     * @throws UnmodifiableClassException
     */
    void transformThreadClass(Consumer<Thread> callback) throws UnmodifiableClassException {
        ClassFileTransformer cf = new ThreadClassTransformer(callback);
        inst.addTransformer(cf, true);
        inst.retransformClasses(Thread.class);
        inst.removeTransformer(cf);
    }

    private void log(String msg) {
        if (options.getDebug()) {
            logger.log("Agent: " + msg);
        }
    }
}
