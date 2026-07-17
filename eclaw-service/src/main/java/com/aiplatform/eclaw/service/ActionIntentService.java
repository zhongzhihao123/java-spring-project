package com.aiplatform.eclaw.service;

import com.aiplatform.eclaw.dto.ActionIntent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class ActionIntentService {

    public ActionIntent detect(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }

        String lower = message.toLowerCase();
        if (lower.contains("npm install") || lower.contains("执行命令") || lower.contains("运行命令")) {
            return buildIntent("command", "执行命令", message, "medium", Map.of("command", message));
        }
        if (lower.contains("调用mcp") || lower.contains("查询数据库") || lower.contains("执行sql")) {
            return buildIntent("mcp_call", "调用 MCP", message, "medium", Map.of("request", message));
        }
        if (lower.contains("请求接口") || lower.contains("调用接口") || lower.contains("http://") || lower.contains("https://")) {
            return buildIntent("http_request", "发起网络请求", message, "medium", Map.of("request", message));
        }
        return null;
    }

    private ActionIntent buildIntent(String type, String title, String summary, String riskLevel, Map<String, Object> payload) {
        ActionIntent intent = new ActionIntent();
        intent.setId("act_" + UUID.randomUUID());
        intent.setType(type);
        intent.setTitle(title);
        intent.setSummary(summary);
        intent.setRiskLevel(riskLevel);
        intent.setSideEffect(true);
        intent.setScope("当前工作区");
        intent.setPayload(payload);
        intent.setPreview(payload);
        return intent;
    }
}
