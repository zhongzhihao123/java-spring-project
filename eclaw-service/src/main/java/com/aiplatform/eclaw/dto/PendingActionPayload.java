package com.aiplatform.eclaw.dto;

import java.util.Map;

public class PendingActionPayload {
    private String id;
    private String type;
    private String title;
    private String summary;
    private String riskLevel;
    private String scope;
    private Map<String, Object> payload;
    private Map<String, Object> preview;

    public static PendingActionPayload from(ActionIntent intent) {
        PendingActionPayload payload = new PendingActionPayload();
        payload.setId(intent.getId());
        payload.setType(intent.getType());
        payload.setTitle(intent.getTitle());
        payload.setSummary(intent.getSummary());
        payload.setRiskLevel(intent.getRiskLevel());
        payload.setScope(intent.getScope());
        payload.setPayload(intent.getPayload());
        payload.setPreview(intent.getPreview());
        return payload;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public Map<String, Object> getPreview() { return preview; }
    public void setPreview(Map<String, Object> preview) { this.preview = preview; }
}
