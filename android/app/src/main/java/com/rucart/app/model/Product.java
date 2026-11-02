package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Product implements Serializable {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String unit;
    private String imageUrl;
    private Integer stock;
    private boolean available;
    private Category category;
    private Store store;

    public Product() {
    }

    public Product(Long id, String name, BigDecimal price, BigDecimal originalPrice, String unit) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.originalPrice = originalPrice;
        this.unit = unit;
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

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    public double getMrp() {
        return originalPrice != null ? originalPrice.doubleValue() : (price != null ? price.doubleValue() * 1.25 : 0.0);
    }

    private Integer stockQuantity;
    private Integer lowStockThreshold;

    public Integer getStockQuantity() {
        return stockQuantity != null ? stockQuantity : (stock != null ? stock : 0);
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
        this.stock = stockQuantity;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold != null ? lowStockThreshold : 10;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public boolean isInStock() {
        return isAvailable() && getStockQuantity() > 0;
    }

    public boolean isLowStock() {
        return isInStock() && getStockQuantity() <= getLowStockThreshold();
    }
}
