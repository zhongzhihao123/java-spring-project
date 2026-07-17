package com.aiplatform.eclaw.service;

import com.aiplatform.eclaw.dto.ActionIntent;
import com.aiplatform.eclaw.dto.ChatResponsePayload;
import com.aiplatform.eclaw.dto.PendingActionPayload;
import org.springframework.stereotype.Service;

@Service
public class ChatModePolicyService {

    public ChatResponsePayload evaluate(String mode, ActionIntent intent, String fallbackMessage) {
        if (intent == null) {
            return ChatResponsePayload.message(fallbackMessage);
        }
        if (!intent.isSideEffect()) {
            return ChatResponsePayload.message(fallbackMessage);
        }
        if ("confirm".equals(mode) || "plan_confirm".equals(mode)) {
            return ChatResponsePayload.approvalRequired("该操作会产生副作用，请先确认。", PendingActionPayload.from(intent));
        }
        if ("plan".equals(mode)) {
            return ChatResponsePayload.message("当前为计划模式，以下动作不会执行：\n" + intent.getTitle());
        }
        return ChatResponsePayload.message(fallbackMessage);
    }
}
