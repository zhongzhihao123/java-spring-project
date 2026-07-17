package com.aiplatform.eclaw.service;

import com.aiplatform.eclaw.dto.PendingActionPayload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ActionExecutionService {

    public Map<String, Object> execute(PendingActionPayload payload) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "executed");
        result.put("content", "已执行动作：" + payload.getTitle() + "\n> " + payload.getSummary());
        return result;
    }
}
