package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RiderCommissionDto implements Serializable {
    private Long id;
    private String commissionType; // FIXED, PERCENTAGE
    private BigDecimal fixedAmount;
    private BigDecimal percentageRate;
    private BigDecimal currentEffectiveCut;
    private String description;
    private String updatedBy;
    private String updatedAt;
    private List<HistoryItemDto> history = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCommissionType() { return commissionType != null ? commissionType : "FIXED"; }
    public void setCommissionType(String commissionType) { this.commissionType = commissionType; }

    public BigDecimal getFixedAmount() { return fixedAmount != null ? fixedAmount : BigDecimal.ZERO; }
    public void setFixedAmount(BigDecimal fixedAmount) { this.fixedAmount = fixedAmount; }

    public BigDecimal getPercentageRate() { return percentageRate != null ? percentageRate : BigDecimal.ZERO; }
    public void setPercentageRate(BigDecimal percentageRate) { this.percentageRate = percentageRate; }

    public BigDecimal getCurrentEffectiveCut() { return currentEffectiveCut != null ? currentEffectiveCut : BigDecimal.ZERO; }
    public void setCurrentEffectiveCut(BigDecimal currentEffectiveCut) { this.currentEffectiveCut = currentEffectiveCut; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<HistoryItemDto> getHistory() { return history; }
    public void setHistory(List<HistoryItemDto> history) { this.history = history; }

    public static class HistoryItemDto implements Serializable {
        private Long id;
        private String commissionType;
        private BigDecimal fixedAmount;
        private BigDecimal percentageRate;
        private String changeReason;
        private String changedBy;
        private String createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCommissionType() { return commissionType; }
        public void setCommissionType(String commissionType) { this.commissionType = commissionType; }

        public BigDecimal getFixedAmount() { return fixedAmount; }
        public void setFixedAmount(BigDecimal fixedAmount) { this.fixedAmount = fixedAmount; }

        public BigDecimal getPercentageRate() { return percentageRate; }
        public void setPercentageRate(BigDecimal percentageRate) { this.percentageRate = percentageRate; }

        public String getChangeReason() { return changeReason; }
        public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

        public String getChangedBy() { return changedBy; }
        public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
