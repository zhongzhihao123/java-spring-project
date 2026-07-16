package com.aiplatform.cicd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CI/CD 流水线实体
 */
@Entity
@Table(name = "cicd_pipelines")
public class Pipeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 20)
    private String status = "active";

    @Column(name = "trigger_type", length = 20)
    private String triggerType = "manual";

    @Column(name = "repo_url", length = 500)
    private String repoUrl;

    @Column(name = "default_branch", length = 100)
    private String defaultBranch = "main";

    @Column(name = "env_list", length = 200)
    private String envList;

    @Column(name = "notify_enabled")
    private Boolean notifyEnabled = false;

    @Column(name = "notify_type", length = 50)
    private String notifyType;

    @Column(name = "notify_targets", length = 500)
    private String notifyTargets;

    @Column(name = "notify_group", length = 500)
    private String notifyGroup;

    @Column(name = "notify_on", length = 100)
    private String notifyOn;

    private Boolean favorite = false;

    @Column(name = "last_exec_at")
    private LocalDateTime lastExecAt;

    @Column(name = "last_status", length = 20)
    private String lastStatus;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "pipeline", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stageOrder ASC")
    private List<Stage> stages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }
    public String getEnvList() { return envList; }
    public void setEnvList(String envList) { this.envList = envList; }
    public Boolean getNotifyEnabled() { return notifyEnabled; }
    public void setNotifyEnabled(Boolean notifyEnabled) { this.notifyEnabled = notifyEnabled; }
    public String getNotifyType() { return notifyType; }
    public void setNotifyType(String notifyType) { this.notifyType = notifyType; }
    public String getNotifyTargets() { return notifyTargets; }
    public void setNotifyTargets(String notifyTargets) { this.notifyTargets = notifyTargets; }
    public String getNotifyGroup() { return notifyGroup; }
    public void setNotifyGroup(String notifyGroup) { this.notifyGroup = notifyGroup; }
    public String getNotifyOn() { return notifyOn; }
    public void setNotifyOn(String notifyOn) { this.notifyOn = notifyOn; }
    public Boolean getFavorite() { return favorite; }
    public void setFavorite(Boolean favorite) { this.favorite = favorite; }
    public LocalDateTime getLastExecAt() { return lastExecAt; }
    public void setLastExecAt(LocalDateTime lastExecAt) { this.lastExecAt = lastExecAt; }
    public String getLastStatus() { return lastStatus; }
    public void setLastStatus(String lastStatus) { this.lastStatus = lastStatus; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Stage> getStages() { return stages; }
    public void setStages(List<Stage> stages) { this.stages = stages; }
}
