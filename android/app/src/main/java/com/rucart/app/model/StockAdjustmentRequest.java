package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class StockAdjustmentRequest implements Serializable {

    private Long productId;
    private Integer newQuantity;
    private Integer changeAmount;
    private BigDecimal newPrice;
    private Integer lowStockThreshold;
    private Boolean available;
    private String reason;

    public StockAdjustmentRequest() {
    }

    public StockAdjustmentRequest(Long productId, Integer newQuantity, String reason) {
        this.productId = productId;
        this.newQuantity = newQuantity;
        this.reason = reason;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public Integer getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Integer changeAmount) {
        this.changeAmount = changeAmount;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(BigDecimal newPrice) {
        this.newPrice = newPrice;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
