package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Offer implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String code;
    private String discountType; // PERCENTAGE, FIXED
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private String startDate;
    private String expiryDate;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDiscountType() { return discountType != null ? discountType : "PERCENTAGE"; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue != null ? discountValue : BigDecimal.ZERO; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public BigDecimal getMinOrderAmount() { return minOrderAmount != null ? minOrderAmount : BigDecimal.ZERO; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
