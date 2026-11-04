package com.rucart.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rider_commission_history")
public class RiderCommissionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommissionType commissionType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionRate;

    private String changedBy;

    private String notes;

    private LocalDateTime changedAt = LocalDateTime.now();

    public RiderCommissionHistory() {
    }

    public RiderCommissionHistory(CommissionType commissionType, BigDecimal commissionRate, String changedBy, String notes) {
        this.commissionType = commissionType;
        this.commissionRate = commissionRate;
        this.changedBy = changedBy;
        this.notes = notes;
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

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
