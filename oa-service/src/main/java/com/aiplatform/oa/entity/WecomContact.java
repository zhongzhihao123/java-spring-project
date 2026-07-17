package com.aiplatform.oa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "oa_wecom_contacts")
public class WecomContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "wecom_userid", length = 100)
    private String wecomUserid;

    @Column(name = "wecom_name", length = 50)
    private String wecomName;

    @Column(name = "wecom_department", length = 100)
    private String wecomDepartment;

    @Column(name = "wecom_position", length = 100)
    private String wecomPosition;

    @Column(name = "wecom_mobile", length = 20)
    private String wecomMobile;

    @Column(name = "wecom_email", length = 100)
    private String wecomEmail;

    @Column(name = "wecom_avatar", length = 500)
    private String wecomAvatar;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getWecomUserid() { return wecomUserid; }
    public void setWecomUserid(String wecomUserid) { this.wecomUserid = wecomUserid; }

    public String getWecomName() { return wecomName; }
    public void setWecomName(String wecomName) { this.wecomName = wecomName; }

    public String getWecomDepartment() { return wecomDepartment; }
    public void setWecomDepartment(String wecomDepartment) { this.wecomDepartment = wecomDepartment; }

    public String getWecomPosition() { return wecomPosition; }
    public void setWecomPosition(String wecomPosition) { this.wecomPosition = wecomPosition; }

    public String getWecomMobile() { return wecomMobile; }
    public void setWecomMobile(String wecomMobile) { this.wecomMobile = wecomMobile; }

    public String getWecomEmail() { return wecomEmail; }
    public void setWecomEmail(String wecomEmail) { this.wecomEmail = wecomEmail; }

    public String getWecomAvatar() { return wecomAvatar; }
    public void setWecomAvatar(String wecomAvatar) { this.wecomAvatar = wecomAvatar; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
