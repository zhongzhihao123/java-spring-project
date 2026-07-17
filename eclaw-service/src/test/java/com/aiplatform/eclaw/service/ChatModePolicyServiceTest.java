package com.aiplatform.eclaw.service;

import com.aiplatform.eclaw.dto.ActionIntent;
import com.aiplatform.eclaw.dto.ChatResponsePayload;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChatModePolicyServiceTest {

    private final ChatModePolicyService policy = new ChatModePolicyService();

    @Test
    void confirmMode_shouldRequireApprovalForSideEffectAction() {
        ActionIntent intent = new ActionIntent();
        intent.setId("act_1");
        intent.setType("command");
        intent.setTitle("执行命令");
        intent.setSideEffect(true);

        ChatResponsePayload response = policy.evaluate("confirm", intent, "说明");

        assertEquals("approval_required", response.getResponseType());
        assertNotNull(response.getPendingAction());
        assertEquals("act_1", response.getPendingAction().getId());
    }

    @Test
    void planMode_shouldReturnPlanMessageWithoutApproval() {
        ActionIntent intent = new ActionIntent();
        intent.setId("act_2");
        intent.setType("command");
        intent.setTitle("执行命令");
        intent.setSideEffect(true);

        ChatResponsePayload response = policy.evaluate("plan", intent, "说明");

        assertEquals("message", response.getResponseType());
        assertEquals(null, response.getPendingAction());
    }
}
