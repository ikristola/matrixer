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

 public interface StackRecorder {

    public void pushMethod(String methodName, long thread);
    public void popMethod(String methodName, long thread);
    public void beginTestCase(String testName, long thread);
    public void endTestCase(String testName, long thread);
    public void newThread(long parentId, Thread newThread);
    public void setDepthLimit(int depthLimit);
    public int activeThreadCount();

 }
