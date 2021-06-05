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
package org.matrixer.core;

import java.io.*;

import org.matrixer.core.runtime.MethodCall;

/**
 * Analyzes data collected with the matrixer agent
 */
public class Analyzer {

    /**
     * Parses execution data from source
     *
     * @param source
     *            A stream containing execution data from the agent
     *
     * @returns the aggregated execution data
     */
    public ExecutionData analyze(InputStream source) {
        ExecutionData data = new ExecutionData();
        BufferedReader stream = new BufferedReader(new InputStreamReader(source));
        stream.lines()
                .filter(line -> line != null && !line.isBlank())
                .map(MethodCall::new)
                .filter(call -> call != null)
                .forEach(data::addCall);
        return data;
    }
}
