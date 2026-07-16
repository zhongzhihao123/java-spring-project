package com.aiplatform.cicd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cicd_exec_stages")
public class ExecStage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "exec_id", nullable = false) private Long execId;
    @Column(name = "stage_name", length = 100) private String stageName;
    @Column(length = 20) private String status = "pending";
    @Column(name = "start_at") private LocalDateTime startAt;
    @Column(name = "end_at") private LocalDateTime endAt;
    @Column(name = "log_text", columnDefinition = "MEDIUMTEXT") private String logText;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExecId() { return execId; }
    public void setExecId(Long execId) { this.execId = execId; }
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public String getLogText() { return logText; }
    public void setLogText(String logText) { this.logText = logText; }
}
