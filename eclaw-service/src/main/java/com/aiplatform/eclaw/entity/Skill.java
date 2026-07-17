package com.aiplatform.eclaw.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "eclaw_skills")
public class Skill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(length = 50)
    private String icon = "🔧";
    @Column(columnDefinition = "TEXT")
    private String prompt;
    @Column(name = "mcp_servers", columnDefinition = "TEXT")
    private String mcpServers;
    @Column(columnDefinition = "TEXT")
    private String tools;
    @Column(name = "trigger_keywords", columnDefinition = "TEXT")
    private String triggerKeywords;
    @Column(name = "is_builtin")
    private Boolean isBuiltin = false;
    @Column(name = "is_enabled")
    private Boolean isEnabled = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; } public void setIcon(String icon) { this.icon = icon; }
    public String getPrompt() { return prompt; } public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getMcpServers() { return mcpServers; } public void setMcpServers(String mcpServers) { this.mcpServers = mcpServers; }
    public String getTools() { return tools; } public void setTools(String tools) { this.tools = tools; }
    public String getTriggerKeywords() { return triggerKeywords; } public void setTriggerKeywords(String triggerKeywords) { this.triggerKeywords = triggerKeywords; }
    public Boolean getIsBuiltin() { return isBuiltin; } public void setIsBuiltin(Boolean isBuiltin) { this.isBuiltin = isBuiltin; }
    public Boolean getIsEnabled() { return isEnabled; } public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
