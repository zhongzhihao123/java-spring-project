package com.aiplatform.eclaw.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name = "eclaw_logs")
public class EclawLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "agent_id") private Long agentId;
    @Column(name = "workflow_id") private Long workflowId;
    @Column(name = "log_type", length = 20) private String logType;
    @Column(columnDefinition = "TEXT") private String request;
    @Column(columnDefinition = "TEXT") private String response;
    @Column(name = "tokens_used") private Integer tokensUsed = 0;
    @Column(name = "latency_ms") private Integer latencyMs;
    @Column(length = 20) private String status;
    @Column(name = "error_msg", length = 1000) private String errorMsg;
    private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getAgentId() { return agentId; } public void setAgentId(Long a) { this.agentId = a; }
    public Long getWorkflowId() { return workflowId; } public void setWorkflowId(Long w) { this.workflowId = w; }
    public String getLogType() { return logType; } public void setLogType(String l) { this.logType = l; }
    public String getRequest() { return request; } public void setRequest(String r) { this.request = r; }
    public String getResponse() { return response; } public void setResponse(String r) { this.response = r; }
    public Integer getTokensUsed() { return tokensUsed; } public void setTokensUsed(Integer t) { this.tokensUsed = t; }
    public Integer getLatencyMs() { return latencyMs; } public void setLatencyMs(Integer l) { this.latencyMs = l; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public String getErrorMsg() { return errorMsg; } public void setErrorMsg(String e) { this.errorMsg = e; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
