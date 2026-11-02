package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class ProductCreateRequest implements Serializable {

    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String unit;
    private String imageUrl;
    private String brand = "Rucart Fresh";
    private Long categoryId;
    private Long storeId;
    private Integer initialStock = 50;
    private Integer lowStockThreshold = 10;
    private Boolean available = true;

    public ProductCreateRequest() {
    }

    public ProductCreateRequest(String name, BigDecimal price, String unit, Long categoryId, Long storeId, Integer initialStock) {
        this.name = name;
        this.price = price;
        this.originalPrice = price.multiply(new BigDecimal("1.25"));
        this.unit = unit;
        this.categoryId = categoryId;
        this.storeId = storeId;
        this.initialStock = initialStock;
        this.available = true;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Integer getInitialStock() {
        return initialStock;
    }

    public void setInitialStock(Integer initialStock) {
        this.initialStock = initialStock;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
