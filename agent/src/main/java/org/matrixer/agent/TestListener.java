package org.matrixer.agent;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

class TestListener implements TestExecutionListener {

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
        System.out.println("Ended a test");
    }

    @Override 
    public void executionStarted(TestIdentifier testIdentifier) {
        System.out.println("Started a test");
    }

}
