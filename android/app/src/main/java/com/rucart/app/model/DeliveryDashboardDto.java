package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class DeliveryDashboardDto implements Serializable {
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

    public String getPartnerName() {
        return partnerName != null ? partnerName : "Delivery Partner";
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getVehicleType() {
        return vehicleType != null ? vehicleType : "Motorcycle";
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber != null ? vehicleNumber : "MH-12-DE-5544";
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Double getRating() {
        return rating != null ? rating : 4.8;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public DeliveryPartnerStatus getStatus() {
        return status != null ? status : DeliveryPartnerStatus.AVAILABLE;
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
        return earningsToday != null ? earningsToday : BigDecimal.ZERO;
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
        return earningsTotal != null ? earningsTotal : BigDecimal.ZERO;
    }

    public void setEarningsTotal(BigDecimal earningsTotal) {
        this.earningsTotal = earningsTotal;
    }
}
