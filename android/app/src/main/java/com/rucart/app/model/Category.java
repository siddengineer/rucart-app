package com.rucart.app.model;

import java.io.Serializable;

public class Category implements Serializable {
    private Long id;
    private String name;
    private String description;
    private String iconName;
    private Integer displayOrder;

    public Category() {
    }

    public Category(Long id, String name, String iconName) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
