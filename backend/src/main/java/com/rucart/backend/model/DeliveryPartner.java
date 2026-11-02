package com.rucart.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_partners")
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String vehicleType = "MOTORCYCLE"; // MOTORCYCLE, SCOOTER, EV

    @Column(length = 30)
    private String vehicleNumber = "MH-12-AB-1234";

    @Column(length = 50)
    private String licenseNumber = "DL-MH12-2022001";

    @Column(nullable = false)
    private boolean available = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryPartnerStatus status = DeliveryPartnerStatus.AVAILABLE;

    @Column(nullable = false)
    private boolean approved = true;

    private Double currentLatitude = 18.5204;
    private Double currentLongitude = 73.8567;

    private Double rating = 4.8;

    private Integer totalDeliveries = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal earningsTotal = BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();

    public DeliveryPartner() {
    }

    public DeliveryPartner(User user, String vehicleType, String vehicleNumber, String licenseNumber) {
        this.user = user;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.licenseNumber = licenseNumber;
        this.available = true;
        this.approved = true;
        this.totalDeliveries = 0;
        this.earningsTotal = BigDecimal.ZERO;
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

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public boolean isAvailable() {
        return available && status == DeliveryPartnerStatus.AVAILABLE;
    }

    public void setAvailable(boolean available) {
        this.available = available;
        if (!available && this.status == DeliveryPartnerStatus.AVAILABLE) {
            this.status = DeliveryPartnerStatus.OFFLINE;
        } else if (available && this.status == DeliveryPartnerStatus.OFFLINE) {
            this.status = DeliveryPartnerStatus.AVAILABLE;
        }
    }

    public DeliveryPartnerStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryPartnerStatus status) {
        this.status = status != null ? status : DeliveryPartnerStatus.AVAILABLE;
        this.available = (this.status == DeliveryPartnerStatus.AVAILABLE);
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
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalDeliveries() {
        return totalDeliveries;
    }

    public void setTotalDeliveries(Integer totalDeliveries) {
        this.totalDeliveries = totalDeliveries;
    }

    public BigDecimal getEarningsTotal() {
        return earningsTotal;
    }

    public void setEarningsTotal(BigDecimal earningsTotal) {
        this.earningsTotal = earningsTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
