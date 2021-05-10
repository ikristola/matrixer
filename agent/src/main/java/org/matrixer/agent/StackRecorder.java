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
