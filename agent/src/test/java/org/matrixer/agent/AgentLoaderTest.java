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
