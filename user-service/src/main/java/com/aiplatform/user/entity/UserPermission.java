package com.aiplatform.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户-权限关联实体（多对多）
 */
@Entity
@Table(name = "user_permissions")
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long permissionId;

    @Column(length = 50)
    private String grantedBy;

    @Column
    private LocalDateTime grantedAt;

    @PrePersist
    protected void onCreate() { grantedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPermissionId() { return permissionId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
    public String getGrantedBy() { return grantedBy; }
    public void setGrantedBy(String grantedBy) { this.grantedBy = grantedBy; }
    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }
}
