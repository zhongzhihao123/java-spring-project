package com.aiplatform.business.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体 — 产品目录中的商品信息
 */
@Entity
@Table(name = "java_products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 商品名称 */
    @Column(nullable = false, length = 200)
    private String name;

    /** 商品描述 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 商品分类 */
    @Column(nullable = false, length = 100)
    private String category;

    /** 价格 */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** 库存数量 */
    @Column(nullable = false)
    private Integer stock = 0;

    /** 商品图片 URL */
    @Column(length = 500)
    private String imageUrl;

    /** 状态: active / inactive */
    @Column(length = 50)
    private String status = "active";

    /** 软删除标记 */
    private Boolean deleted = false;

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
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
