package com.rucart.backend.dto;

import com.rucart.backend.model.DeliveryPartnerStatus;
import java.math.BigDecimal;

public class DeliveryDashboardDto {
    private String partnerName;
    private String vehicleType;
    private String vehicleNumber;
    private Double rating;
    private DeliveryPartnerStatus status;
    private int activeAssignedDeliveriesCount;
    private int completedTodayCount;
    private long pendingPickupsCount;
    private long deliveryIssuesCount;
    private BigDecimal earningsToday;
    private int totalDeliveries;
    private BigDecimal earningsTotal;

    public DeliveryDashboardDto() {
    }

    public DeliveryDashboardDto(String partnerName, String vehicleType, String vehicleNumber, Double rating,
                                DeliveryPartnerStatus status, int activeAssignedDeliveriesCount,
                                int completedTodayCount, long pendingPickupsCount, long deliveryIssuesCount,
                                BigDecimal earningsToday, int totalDeliveries, BigDecimal earningsTotal) {
        this.partnerName = partnerName;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.rating = rating != null ? rating : 4.8;
        this.status = status != null ? status : DeliveryPartnerStatus.AVAILABLE;
        this.activeAssignedDeliveriesCount = activeAssignedDeliveriesCount;
        this.completedTodayCount = completedTodayCount;
        this.pendingPickupsCount = pendingPickupsCount;
        this.deliveryIssuesCount = deliveryIssuesCount;
        this.earningsToday = earningsToday != null ? earningsToday : BigDecimal.ZERO;
        this.totalDeliveries = totalDeliveries;
        this.earningsTotal = earningsTotal != null ? earningsTotal : BigDecimal.ZERO;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public DeliveryPartnerStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryPartnerStatus status) {
        this.status = status;
    }

    public int getActiveAssignedDeliveriesCount() {
        return activeAssignedDeliveriesCount;
    }

    public void setActiveAssignedDeliveriesCount(int activeAssignedDeliveriesCount) {
        this.activeAssignedDeliveriesCount = activeAssignedDeliveriesCount;
    }

    public int getCompletedTodayCount() {
        return completedTodayCount;
    }

    public void setCompletedTodayCount(int completedTodayCount) {
        this.completedTodayCount = completedTodayCount;
    }

    public long getPendingPickupsCount() {
        return pendingPickupsCount;
    }

    public void setPendingPickupsCount(long pendingPickupsCount) {
        this.pendingPickupsCount = pendingPickupsCount;
    }

    public long getDeliveryIssuesCount() {
        return deliveryIssuesCount;
    }

    public void setDeliveryIssuesCount(long deliveryIssuesCount) {
        this.deliveryIssuesCount = deliveryIssuesCount;
    }

    public BigDecimal getEarningsToday() {
        return earningsToday;
    }

    public void setEarningsToday(BigDecimal earningsToday) {
        this.earningsToday = earningsToday;
    }

    public int getTotalDeliveries() {
        return totalDeliveries;
    }

    public void setTotalDeliveries(int totalDeliveries) {
        this.totalDeliveries = totalDeliveries;
    }

    public BigDecimal getEarningsTotal() {
        return earningsTotal;
    }

    public void setEarningsTotal(BigDecimal earningsTotal) {
        this.earningsTotal = earningsTotal;
    }
}
