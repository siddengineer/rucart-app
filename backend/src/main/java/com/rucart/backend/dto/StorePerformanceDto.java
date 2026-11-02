package com.rucart.backend.dto;

import java.math.BigDecimal;

public class StorePerformanceDto {
    private Long storeId;
    private String storeName;
    private String city;
    private boolean active;
    private boolean open;
    private long totalOrders;
    private long deliveredOrders;
    private BigDecimal totalSales;

    public StorePerformanceDto() {
    }

    public StorePerformanceDto(Long storeId, String storeName, String city, boolean active, boolean open,
                               long totalOrders, long deliveredOrders, BigDecimal totalSales) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.city = city;
        this.active = active;
        this.open = open;
        this.totalOrders = totalOrders;
        this.deliveredOrders = deliveredOrders;
        this.totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(long deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }
}
