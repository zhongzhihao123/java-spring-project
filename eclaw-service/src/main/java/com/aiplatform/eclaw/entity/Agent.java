package com.aiplatform.eclaw.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name = "eclaw_agents")
public class Agent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Column(length = 500) private String description;
    @Column(length = 50) private String avatar = "🤖";
    @Column(name = "model_id") private Long modelId;
    @Column(name = "system_prompt", columnDefinition = "TEXT") private String systemPrompt;
    @Column(name = "default_chat_mode", length = 32, nullable = false) private String defaultChatMode = "standard";
    @Column(name = "mcp_servers", columnDefinition = "TEXT") private String mcpServers;
    @Column(name = "skill_ids", columnDefinition = "TEXT") private String skillIds;
    @Column(columnDefinition = "TEXT") private String tools;
    @Column(name = "knowledge_base", length = 500) private String knowledgeBase;
    @Column(length = 20) private String status = "stopped";
    @Column(name = "creator_id") private Long creatorId;
    @Column(name = "creator_name", length = 50) private String creatorName;
    private LocalDateTime createdAt; private LocalDateTime updatedAt;
    @PrePersist void pre() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void up() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public String getAvatar() { return avatar; } public void setAvatar(String a) { this.avatar = a; }
    public Long getModelId() { return modelId; } public void setModelId(Long m) { this.modelId = m; }
    public String getSystemPrompt() { return systemPrompt; } public void setSystemPrompt(String s) { this.systemPrompt = s; }
    public String getDefaultChatMode() { return defaultChatMode; } public void setDefaultChatMode(String defaultChatMode) { this.defaultChatMode = defaultChatMode; }
    public String getMcpServers() { return mcpServers; } public void setMcpServers(String m) { this.mcpServers = m; }
    public String getSkillIds() { return skillIds; } public void setSkillIds(String s) { this.skillIds = s; }
    public String getTools() { return tools; } public void setTools(String t) { this.tools = t; }
    public String getKnowledgeBase() { return knowledgeBase; } public void setKnowledgeBase(String k) { this.knowledgeBase = k; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public Long getCreatorId() { return creatorId; } public void setCreatorId(Long c) { this.creatorId = c; }
    public String getCreatorName() { return creatorName; } public void setCreatorName(String c) { this.creatorName = c; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime u) { this.updatedAt = u; }
}
