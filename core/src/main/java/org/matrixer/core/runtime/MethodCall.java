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
package org.matrixer.core.runtime;

/**
 * Stores information about a method call
 */
public class MethodCall {

    public static final String sep = "#";

    /**
     * The call stack depth of the call
     */
    public final int depth;

    /**
     * The name of the called method
     */
    public final String methodName;

    /**
     * The name of the caller
     */
    public final String callerName;

    public MethodCall(String line) {
        String[] parts = line.split(sep);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Bad method call line format: " + line);
        }
        try {
            depth = Integer.parseInt(parts[0]);
            methodName = parts[1];
            callerName = parts[2];
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad method call line format: " + line);
        }
    }

    /**
     * Creates a new MethodCall
     *
     * @param depth
     *            the depth of the call
     * @param methodName
     *            the name of the called method
     * @param callerName
     *            the name of the caller
     */
    public MethodCall(int depth, String methodName, String callerName) {
        this.depth = depth;
        this.methodName = methodName;
        this.callerName = callerName;
    }

    public String asLine() {
        return depth + sep + methodName + sep + callerName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callerName == null) ? 0 : callerName.hashCode());
        result = prime * result + depth;
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodCall other = (MethodCall) obj;
        if (callerName == null) {
            if (other.callerName != null)
                return false;
        } else if (!callerName.equals(other.callerName))
            return false;
        if (depth != other.depth)
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        return true;
    }

}
