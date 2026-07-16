package com.aiplatform.cicd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cicd_executions")
public class Execution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "pipeline_id", nullable = false) private Long pipelineId;
    @Column(name = "build_no", nullable = false) private Integer buildNo;
    @Column(length = 20) private String status = "pending";
    @Column(name = "trigger_user", length = 50) private String triggerUser;
    @Column(name = "trigger_type", length = 20) private String triggerType;
    @Column(length = 100) private String branch;
    @Column(name = "commit_id", length = 64) private String commitId;
    @Column(name = "commit_msg", length = 500) private String commitMsg;
    @Column(name = "start_at") private LocalDateTime startAt;
    @Column(name = "end_at") private LocalDateTime endAt;
    private Long duration;
    @Column(name = "artifact_count") private Integer artifactCount = 0;
    @Column(name = "artifact_size", length = 50) private String artifactSize;
    @Column(name = "error_code", length = 50) private String errorCode;
    @Column(length = 500) private String remark;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Transient field for pipeline name
    @Transient private String pipelineName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPipelineId() { return pipelineId; }
    public void setPipelineId(Long pipelineId) { this.pipelineId = pipelineId; }
    public Integer getBuildNo() { return buildNo; }
    public void setBuildNo(Integer buildNo) { this.buildNo = buildNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTriggerUser() { return triggerUser; }
    public void setTriggerUser(String triggerUser) { this.triggerUser = triggerUser; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }
    public String getCommitMsg() { return commitMsg; }
    public void setCommitMsg(String commitMsg) { this.commitMsg = commitMsg; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    public Integer getArtifactCount() { return artifactCount; }
    public void setArtifactCount(Integer artifactCount) { this.artifactCount = artifactCount; }
    public String getArtifactSize() { return artifactSize; }
    public void setArtifactSize(String artifactSize) { this.artifactSize = artifactSize; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getPipelineName() { return pipelineName; }
    public void setPipelineName(String pipelineName) { this.pipelineName = pipelineName; }
}
