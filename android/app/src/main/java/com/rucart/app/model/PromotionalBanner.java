package com.rucart.app.model;

import java.io.Serializable;

public class PromotionalBanner implements Serializable {
    private Long id;
    private String title;
    private String imageUrl;
    private String targetType;
    private String targetValue;
    private int displayOrder;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
