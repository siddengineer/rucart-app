package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class DeliveryPartner implements Serializable {
    private Long id;
    private User user;
    private String vehicleType;
    private String vehicleNumber;
    private String licenseNumber;
    private boolean available;
    private DeliveryPartnerStatus status;
    private boolean approved;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double rating;
    private Integer totalDeliveries;
    private BigDecimal earningsTotal;

    public DeliveryPartner() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getVehicleType() {
        return vehicleType != null ? vehicleType : "MOTORCYCLE";
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber != null ? vehicleNumber : "";
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public DeliveryPartnerStatus getStatus() {
        return status != null ? status : DeliveryPartnerStatus.AVAILABLE;
    }

    public void setStatus(DeliveryPartnerStatus status) {
        this.status = status;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public Double getRating() {
        return rating != null ? rating : 4.8;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalDeliveries() {
        return totalDeliveries != null ? totalDeliveries : 0;
    }

    public void setTotalDeliveries(Integer totalDeliveries) {
        this.totalDeliveries = totalDeliveries;
    }

    public BigDecimal getEarningsTotal() {
        return earningsTotal != null ? earningsTotal : BigDecimal.ZERO;
    }

    public void setEarningsTotal(BigDecimal earningsTotal) {
        this.earningsTotal = earningsTotal;
    }
}
