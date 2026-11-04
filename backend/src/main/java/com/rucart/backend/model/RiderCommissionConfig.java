package com.rucart.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rider_commission_configs")
public class RiderCommissionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommissionType commissionType = CommissionType.FIXED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionRate = new BigDecimal("35.00");

    @Column(nullable = false)
    private boolean active = true;

    private String updatedBy = "ADMIN";

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public RiderCommissionConfig() {
    }

    public RiderCommissionConfig(CommissionType commissionType, BigDecimal commissionRate, String updatedBy) {
        this.commissionType = commissionType;
        this.commissionRate = commissionRate;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
