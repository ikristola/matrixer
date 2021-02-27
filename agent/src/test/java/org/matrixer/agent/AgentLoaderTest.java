package org.matrixer.agent;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.matrixer.agent.util.CustomTestAgent;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgentLoaderTest {

    @Test
    public void expectException_loadWithBadPath() {
        Throwable throwable = assertThrows(Exception.class,
                () -> AgentLoader.loadAgent("bad/path/agent.jar", "argument"));
        assertTrue(throwable.getMessage().contains("Agent"));
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
