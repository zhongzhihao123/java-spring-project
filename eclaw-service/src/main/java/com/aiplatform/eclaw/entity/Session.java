package com.aiplatform.eclaw.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name = "eclaw_sessions")
public class Session {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "agent_id", nullable = false) private Long agentId;
    @Column(length = 200) private String title = "新对话";
    @Column(name = "user_id") private Long userId;
    @Column(name = "user_name", length = 50) private String userName;
    @Column(columnDefinition = "TEXT") private String messages;
    @Column(name = "session_mode", length = 32, nullable = false) private String sessionMode = "standard";
    @Column(name = "pending_action", columnDefinition = "TEXT") private String pendingAction;
    @Column(name = "pending_action_status", length = 20, nullable = false) private String pendingActionStatus = "none";
    @Column(name = "total_tokens") private Integer totalTokens = 0;
    @Column(length = 20) private String status = "active";
    private LocalDateTime createdAt; private LocalDateTime updatedAt;
    @PrePersist void pre() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void up() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getAgentId() { return agentId; } public void setAgentId(Long a) { this.agentId = a; }
    public String getTitle() { return title; } public void setTitle(String t) { this.title = t; }
    public Long getUserId() { return userId; } public void setUserId(Long u) { this.userId = u; }
    public String getUserName() { return userName; } public void setUserName(String u) { this.userName = u; }
    public String getMessages() { return messages; } public void setMessages(String m) { this.messages = m; }
    public String getSessionMode() { return sessionMode; } public void setSessionMode(String sessionMode) { this.sessionMode = sessionMode; }
    public String getPendingAction() { return pendingAction; } public void setPendingAction(String pendingAction) { this.pendingAction = pendingAction; }
    public String getPendingActionStatus() { return pendingActionStatus; } public void setPendingActionStatus(String pendingActionStatus) { this.pendingActionStatus = pendingActionStatus; }
    public Integer getTotalTokens() { return totalTokens; } public void setTotalTokens(Integer t) { this.totalTokens = t; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime u) { this.updatedAt = u; }
}
