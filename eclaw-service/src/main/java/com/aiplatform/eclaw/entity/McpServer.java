package com.aiplatform.eclaw.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "eclaw_mcp_servers")
public class McpServer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(length = 20)
    private String transport = "stdio";
    @Column(length = 200)
    private String command;
    @Column(name = "server_url", length = 300)
    private String serverUrl;
    @Column(length = 20)
    private String status = "disconnected";
    @Column(name = "tools_config")
    private String toolsConfig;
    @Column(name = "is_enabled")
    private Boolean isEnabled = true;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getTransport() { return transport; } public void setTransport(String transport) { this.transport = transport; }
    public String getCommand() { return command; } public void setCommand(String command) { this.command = command; }
    public String getServerUrl() { return serverUrl; } public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getToolsConfig() { return toolsConfig; } public void setToolsConfig(String toolsConfig) { this.toolsConfig = toolsConfig; }
    public Boolean getIsEnabled() { return isEnabled; } public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
