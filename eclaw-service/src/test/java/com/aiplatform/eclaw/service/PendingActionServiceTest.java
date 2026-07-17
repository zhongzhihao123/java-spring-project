package com.aiplatform.eclaw.service;

import com.aiplatform.eclaw.dto.PendingActionPayload;
import com.aiplatform.eclaw.entity.Session;
import com.aiplatform.eclaw.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PendingActionServiceTest {

    @Mock
    private SessionRepository sessionRepo;

    @Mock
    private ActionExecutionService actionExecutionService;

    @InjectMocks
    private PendingActionService pendingActionService;

    @Test
    void confirmAction_shouldBeIdempotent() {
        Session session = new Session();
        session.setId(9L);
        session.setPendingAction("{\"id\":\"act_1\",\"type\":\"command\",\"title\":\"执行命令\",\"summary\":\"运行命令\"}");
        session.setPendingActionStatus("waiting");

        when(sessionRepo.findById(9L)).thenReturn(Optional.of(session));
        when(sessionRepo.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(actionExecutionService.execute(any(PendingActionPayload.class)))
            .thenReturn(Map.of("status", "executed", "content", "已执行"));

        Map<String, Object> first = pendingActionService.confirmAndExecute(9L, "act_1");

        assertEquals("已执行", first.get("content"));
        assertThrows(IllegalStateException.class, () -> pendingActionService.confirmAndExecute(9L, "act_1"));
    }

    @Test
    void cancelAction_shouldClearPendingState() {
        Session session = new Session();
        session.setId(10L);
        session.setPendingAction("{\"id\":\"act_2\",\"type\":\"command\",\"title\":\"执行命令\",\"summary\":\"运行命令\"}");
        session.setPendingActionStatus("waiting");

        when(sessionRepo.findById(10L)).thenReturn(Optional.of(session));
        when(sessionRepo.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = pendingActionService.cancelAsMessage(10L, "act_2");

        assertEquals("已取消待确认动作。", result.get("content"));
        assertEquals("cancelled", session.getPendingActionStatus());
        assertEquals(null, session.getPendingAction());
    }
}
