package com.rucart.backend.dto;

import java.math.BigDecimal;

public class LowStockAlertDto {

    private Long storeId;
    private String storeName;
    private Long productId;
    private String productName;
    private String categoryName;
    private String unit;
    private BigDecimal price;
    private Integer currentStock;
    private Integer threshold;
    private boolean outOfStock;

    public LowStockAlertDto() {
    }

    public LowStockAlertDto(Long storeId, String storeName, Long productId, String productName,
                            String categoryName, String unit, BigDecimal price,
                            Integer currentStock, Integer threshold) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.unit = unit;
        this.price = price;
        this.currentStock = currentStock;
        this.threshold = threshold;
        this.outOfStock = currentStock != null && currentStock <= 0;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
        this.outOfStock = currentStock != null && currentStock <= 0;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public boolean isOutOfStock() {
        return outOfStock;
    }

    public void setOutOfStock(boolean outOfStock) {
        this.outOfStock = outOfStock;
    }
}
