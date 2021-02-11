package org.matrixeragent;

import com.sun.tools.attach.AgentLoadException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.matrixeragent.util.CustomTestAgent;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgentLoaderTest {

    @Test
    public void expectException_loadWithBadPath() {
        Throwable throwable = assertThrows(AgentLoadException.class,
                () -> AgentLoader.loadAgent("bad/path/agent.jar", "argument"));
        assertEquals("Agent JAR not found or no Agent-Class attribute", throwable.getMessage());
    }

    @Test
    @Order(1)
    public void agentIsNotRunningBeforeLoad() {
        Throwable throwable = assertThrows(IllegalStateException.class,
                CustomTestAgent::getInstance);
        assertEquals("The agent is not loaded!", throwable.getMessage());
    }

    @Test
    @Order(2)
    public void loadAgent() {
        assertDoesNotThrow(() -> AgentLoader.loadAgent("build/libs/testAgentJar.jar", "argument"));
    }

    @Test
    @Order(3)
    public void agentIsRunningAfterLoad() {
        assertDoesNotThrow(CustomTestAgent::getInstance);
    }
}