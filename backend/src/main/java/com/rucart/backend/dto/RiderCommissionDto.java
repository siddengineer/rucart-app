package com.rucart.backend.dto;

import com.rucart.backend.model.CommissionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RiderCommissionDto {

    private CommissionType commissionType;
    private BigDecimal commissionRate;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public RiderCommissionDto() {
    }

    public RiderCommissionDto(CommissionType commissionType, BigDecimal commissionRate, String updatedBy, LocalDateTime updatedAt) {
        this.commissionType = commissionType;
        this.commissionRate = commissionRate;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public CommissionType getCommissionType() {
        return commissionType;
    }

    public void setCommissionType(CommissionType commissionType) {
        this.commissionType = commissionType;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
