package com.aiplatform.cicd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cicd_exec_steps")
public class ExecStep {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "exec_stage_id", nullable = false) private Long execStageId;
    @Column(name = "step_name", length = 100) private String stepName;
    @Column(length = 20) private String status = "pending";
    @Column(name = "start_at") private LocalDateTime startAt;
    @Column(name = "end_at") private LocalDateTime endAt;
    @Column(name = "log_text", columnDefinition = "MEDIUMTEXT") private String logText;
    @Column(name = "exit_code") private Integer exitCode = -1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExecStageId() { return execStageId; }
    public void setExecStageId(Long execStageId) { this.execStageId = execStageId; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public String getLogText() { return logText; }
    public void setLogText(String logText) { this.logText = logText; }
    public Integer getExitCode() { return exitCode; }
    public void setExitCode(Integer exitCode) { this.exitCode = exitCode; }
}
