package com.aiplatform.cicd.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cicd_steps")
public class Step {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "stage_id", nullable = false) @JsonIgnore
    private Stage stage;
    @Column(nullable = false, length = 100) private String name;
    @Column(name = "step_order", nullable = false) private Integer stepOrder;
    @Column(name = "step_type", length = 20) private String stepType = "shell";
    @Column(name = "command_text", columnDefinition = "TEXT") private String commandText;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    public String getStepType() { return stepType; }
    public void setStepType(String stepType) { this.stepType = stepType; }
    public String getCommandText() { return commandText; }
    public void setCommandText(String commandText) { this.commandText = commandText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
