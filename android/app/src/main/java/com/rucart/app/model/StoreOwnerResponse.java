package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class StoreOwnerResponse implements Serializable {
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private boolean active;
    private Long storeId;
    private String storeName;
    private String storeArea;
    private String generatedPassword;
    private int totalOrders;
    private BigDecimal totalSales;
    private String createdAt;

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

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreArea() { return storeArea; }
    public void setStoreArea(String storeArea) { this.storeArea = storeArea; }

    public String getGeneratedPassword() { return generatedPassword; }
    public void setGeneratedPassword(String generatedPassword) { this.generatedPassword = generatedPassword; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalSales() { return totalSales != null ? totalSales : BigDecimal.ZERO; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
