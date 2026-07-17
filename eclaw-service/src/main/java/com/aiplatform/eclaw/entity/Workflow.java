package com.aiplatform.eclaw.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name = "eclaw_workflows")
public class Workflow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Column(length = 500) private String description;
    @Column(name = "workflow_type", length = 20) private String workflowType = "visual";
    @Column(name = "flow_config", columnDefinition = "TEXT") private String flowConfig;
    @Column(length = 20) private String status = "draft";
    @Column(name = "creator_id") private Long creatorId;
    @Column(name = "creator_name", length = 50) private String creatorName;
    private LocalDateTime createdAt; private LocalDateTime updatedAt;
    @PrePersist void pre() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void up() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String n) { this.name = n; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public String getWorkflowType() { return workflowType; } public void setWorkflowType(String w) { this.workflowType = w; }
    public String getFlowConfig() { return flowConfig; } public void setFlowConfig(String f) { this.flowConfig = f; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public Long getCreatorId() { return creatorId; } public void setCreatorId(Long c) { this.creatorId = c; }
    public String getCreatorName() { return creatorName; } public void setCreatorName(String c) { this.creatorName = c; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime u) { this.updatedAt = u; }
}
