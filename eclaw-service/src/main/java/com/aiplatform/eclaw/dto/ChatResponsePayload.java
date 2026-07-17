package com.aiplatform.eclaw.dto;

import java.util.HashMap;
import java.util.Map;

public class ChatResponsePayload {
    private String responseType;
    private String content;
    private Integer tokensUsed;
    private String model;
    private Long sessionId;
    private String sessionMode;
    private PendingActionPayload pendingAction;

    public static ChatResponsePayload message(String content) {
        ChatResponsePayload payload = new ChatResponsePayload();
        payload.setResponseType("message");
        payload.setContent(content);
        return payload;
    }

    public static ChatResponsePayload approvalRequired(String content, PendingActionPayload pendingAction) {
        ChatResponsePayload payload = new ChatResponsePayload();
        payload.setResponseType("approval_required");
        payload.setContent(content);
        payload.setPendingAction(pendingAction);
        return payload;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("responseType", responseType);
        map.put("content", content);
        map.put("tokensUsed", tokensUsed);
        map.put("model", model);
        map.put("sessionId", sessionId);
        map.put("sessionMode", sessionMode);
        map.put("pendingAction", pendingAction);
        return map;
    }

    public String getResponseType() { return responseType; }
    public void setResponseType(String responseType) { this.responseType = responseType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getSessionMode() { return sessionMode; }
    public void setSessionMode(String sessionMode) { this.sessionMode = sessionMode; }
    public PendingActionPayload getPendingAction() { return pendingAction; }
    public void setPendingAction(PendingActionPayload pendingAction) { this.pendingAction = pendingAction; }
}
