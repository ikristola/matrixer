package org.matrixer.agent;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.matrixer.agent.util.CustomTestAgent;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgentLoaderTest {

    @Test
    public void expectException_loadWithBadPath() {
        Throwable throwable = assertThrows(Exception.class,
                () -> AgentLoader.loadAgent("bad/path/agent.jar", "argument"));
        assertEquals("Agent JAR not found or no Agent-Class attribute",
                throwable.getMessage());
    }

    @Test
    @Order(1)
    public void loadAgent() {
        assertDoesNotThrow(() -> AgentLoader
                .loadAgent("build/libs/testAgentJar.jar", "argument"));
    }

    @Test
    @Order(2)
    public void agentIsRunningAfterLoad() {
        assertDoesNotThrow(CustomTestAgent::getInstance);
    }
}
