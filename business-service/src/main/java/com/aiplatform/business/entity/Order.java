package com.aiplatform.business.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体 — 用户下单记录
 */
@Entity
@Table(name = "java_orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单号（唯一） */
    @Column(nullable = false, length = 64, unique = true)
    private String orderNo;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /** 订单状态: pending / paid / shipped / completed / cancelled */
    @Column(length = 20)
    private String status = "pending";

    /** 备注 */
    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // ── Getters & Setters ──
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
