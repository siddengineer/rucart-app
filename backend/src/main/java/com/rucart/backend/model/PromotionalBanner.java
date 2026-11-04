package com.rucart.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotional_banners")
public class PromotionalBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 50)
    private String targetType = "CATEGORY"; // CATEGORY, PRODUCT, STORE, EXTERNAL

    @Column(length = 255)
    private String targetValue;

    private int displayOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PromotionalBanner() {
    }

    public PromotionalBanner(String title, String imageUrl, String targetType, String targetValue, int displayOrder) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.targetType = targetType;
        this.targetValue = targetValue;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
