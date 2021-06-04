package org.matrixer.agent.internal;

public class Call {
    public final String calledMethod;
    public final int stackDepth;

    public Call(String calledMethod, int stackDepth) {
        this.calledMethod = calledMethod;
        this.stackDepth = stackDepth;
    }
}
