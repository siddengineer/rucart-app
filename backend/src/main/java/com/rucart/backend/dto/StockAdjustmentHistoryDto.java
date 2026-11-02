package com.rucart.backend.dto;

import com.rucart.backend.model.StockAdjustmentType;
import java.time.LocalDateTime;

public class StockAdjustmentHistoryDto {

    private Long id;
    private Long storeId;
    private String storeName;
    private Long productId;
    private String productName;
    private Integer previousQuantity;
    private Integer newQuantity;
    private Integer quantityChange;
    private StockAdjustmentType adjustmentType;
    private String reason;
    private String adjustedByName;
    private LocalDateTime createdAt;

    public StockAdjustmentHistoryDto() {
    }

    public StockAdjustmentHistoryDto(Long id, Long storeId, String storeName, Long productId,
                                     String productName, Integer previousQuantity, Integer newQuantity,
                                     Integer quantityChange, StockAdjustmentType adjustmentType,
                                     String reason, String adjustedByName, LocalDateTime createdAt) {
        this.id = id;
        this.storeId = storeId;
        this.storeName = storeName;
        this.productId = productId;
        this.productName = productName;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.quantityChange = quantityChange;
        this.adjustmentType = adjustmentType;
        this.reason = reason;
        this.adjustedByName = adjustedByName;
        this.createdAt = createdAt;
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

    public StockAdjustmentType getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(StockAdjustmentType adjustmentType) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
