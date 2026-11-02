package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class StoreOrderStatsDto implements Serializable {
    private Long storeId;
    private String storeName;
    private boolean open;
    private boolean active;
    private long newOrdersCount;
    private long preparingOrdersCount;
    private long readyOrdersCount;
    private long completedOrdersCount;
    private long totalOrdersCount;
    private BigDecimal totalSales;

    public StoreOrderStatsDto() {
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

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getNewOrdersCount() {
        return newOrdersCount;
    }

    public void setNewOrdersCount(long newOrdersCount) {
        this.newOrdersCount = newOrdersCount;
    }

    public long getPreparingOrdersCount() {
        return preparingOrdersCount;
    }

    public void setPreparingOrdersCount(long preparingOrdersCount) {
        this.preparingOrdersCount = preparingOrdersCount;
    }

    public long getReadyOrdersCount() {
        return readyOrdersCount;
    }

    public void setReadyOrdersCount(long readyOrdersCount) {
        this.readyOrdersCount = readyOrdersCount;
    }

    public long getCompletedOrdersCount() {
        return completedOrdersCount;
    }

    public void setCompletedOrdersCount(long completedOrdersCount) {
        this.completedOrdersCount = completedOrdersCount;
    }

    public long getTotalOrdersCount() {
        return totalOrdersCount;
    }

    public void setTotalOrdersCount(long totalOrdersCount) {
        this.totalOrdersCount = totalOrdersCount;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }
}
