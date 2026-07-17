package com.aiplatform.eclaw.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eclaw_models")
public class ModelConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, length = 30)
    private String provider;
    @Column(name = "model_id", nullable = false, length = 100)
    private String modelId;
    @Column(name = "api_base", length = 300)
    private String apiBase;
    @Column(name = "api_key", length = 200)
    private String apiKey;
    @Column(name = "max_tokens")
    private Integer maxTokens = 4096;
    private Double temperature = 0.7;
    @Column(name = "is_enabled")
    private Boolean isEnabled = true;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters/Setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getProvider() { return provider; } public void setProvider(String provider) { this.provider = provider; }
    public String getModelId() { return modelId; } public void setModelId(String modelId) { this.modelId = modelId; }
    public String getApiBase() { return apiBase; } public void setApiBase(String apiBase) { this.apiBase = apiBase; }
    public String getApiKey() { return apiKey; } public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public Integer getMaxTokens() { return maxTokens; } public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    public Double getTemperature() { return temperature; } public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Boolean getIsEnabled() { return isEnabled; } public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
