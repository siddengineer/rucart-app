package com.rucart.app.model;

import java.io.Serializable;

public class CategoryRequest implements Serializable {
    private String name;
    private String description;
    private String iconName;
    private String imageUrl;
    private Integer displayOrder = 1;

    public CategoryRequest() {
    }

    public CategoryRequest(String name, String description, String iconName, String imageUrl, Integer displayOrder) {
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
