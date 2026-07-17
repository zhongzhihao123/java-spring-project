package com.aiplatform.eclaw.service;

import com.aiplatform.eclaw.entity.Agent;
import com.aiplatform.eclaw.entity.Session;
import com.aiplatform.eclaw.repository.AgentRepository;
import com.aiplatform.eclaw.repository.EclawLogRepository;
import com.aiplatform.eclaw.repository.McpServerRepository;
import com.aiplatform.eclaw.repository.ModelConfigRepository;
import com.aiplatform.eclaw.repository.SessionRepository;
import com.aiplatform.eclaw.repository.SkillRepository;
import com.aiplatform.eclaw.repository.WorkflowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EclawServiceSessionModeTest {

    @Mock private ModelConfigRepository modelRepo;
    @Mock private McpServerRepository mcpRepo;
    @Mock private SkillRepository skillRepo;
    @Mock private AgentRepository agentRepo;
    @Mock private WorkflowRepository workflowRepo;
    @Mock private SessionRepository sessionRepo;
    @Mock private EclawLogRepository logRepo;

    @InjectMocks
    private EclawService service;

    @Test
    void createSession_shouldCopyAgentDefaultChatMode() {
        Agent agent = new Agent();
        agent.setId(7L);
        agent.setDefaultChatMode("plan_confirm");

        when(agentRepo.findById(7L)).thenReturn(Optional.of(agent));
        when(sessionRepo.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Session created = service.createSession(7L, 1L, "alice");

        assertEquals("plan_confirm", created.getSessionMode());
    }

    @Test
    void updateSession_shouldPersistModeAndPendingActionFields() {
        Session session = new Session();
        session.setId(11L);
        session.setSessionMode("standard");
        session.setPendingActionStatus("none");

        when(sessionRepo.findById(11L)).thenReturn(Optional.of(session));
        when(sessionRepo.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Session updated = service.updateSession(11L, null, "[]", 15, "confirm", "{\"id\":\"act_1\"}", "waiting");

        assertEquals("confirm", updated.getSessionMode());
        assertEquals("{\"id\":\"act_1\"}", updated.getPendingAction());
        assertEquals("waiting", updated.getPendingActionStatus());
    }
}
