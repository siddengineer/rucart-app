package com.rucart.app.model;

import java.io.Serializable;

public class StockAdjustmentHistoryDto implements Serializable {

    private Long id;
    private Long storeId;
    private String storeName;
    private Long productId;
    private String productName;
    private Integer previousQuantity;
    private Integer newQuantity;
    private Integer quantityChange;
    private String adjustmentType;
    private String reason;
    private String adjustedByName;
    private String referenceOrderNumber;
    private String createdAt;

    public StockAdjustmentHistoryDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getPreviousQuantity() {
        return previousQuantity;
    }

    public void setPreviousQuantity(Integer previousQuantity) {
        this.previousQuantity = previousQuantity;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAdjustedByName() {
        return adjustedByName;
    }

    public void setAdjustedByName(String adjustedByName) {
        this.adjustedByName = adjustedByName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getReferenceOrderNumber() {
        return referenceOrderNumber;
    }

    public void setReferenceOrderNumber(String referenceOrderNumber) {
        this.referenceOrderNumber = referenceOrderNumber;
    }

    public Integer getPreviousStock() {
        return previousQuantity;
    }

    public void setPreviousStock(Integer previousStock) {
        this.previousQuantity = previousStock;
    }

    public Integer getNewStock() {
        return newQuantity;
    }

    public void setNewStock(Integer newStock) {
        this.newQuantity = newStock;
    }
}
