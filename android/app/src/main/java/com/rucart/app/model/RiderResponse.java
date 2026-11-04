package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class RiderResponse implements Serializable {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String vehicleType;
    private String vehicleNumber;
    private String status;
    private boolean active;
    private Integer totalDeliveries;
    private BigDecimal totalDeliveredAmount;
    private String generatedPassword;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Integer getTotalDeliveries() { return totalDeliveries != null ? totalDeliveries : 0; }
    public void setTotalDeliveries(Integer totalDeliveries) { this.totalDeliveries = totalDeliveries; }

    public BigDecimal getTotalDeliveredAmount() { return totalDeliveredAmount != null ? totalDeliveredAmount : BigDecimal.ZERO; }
    public void setTotalDeliveredAmount(BigDecimal totalDeliveredAmount) { this.totalDeliveredAmount = totalDeliveredAmount; }

    public String getGeneratedPassword() { return generatedPassword; }
    public void setGeneratedPassword(String generatedPassword) { this.generatedPassword = generatedPassword; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
