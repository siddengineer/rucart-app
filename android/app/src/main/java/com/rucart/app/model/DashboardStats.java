package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class DashboardStats implements Serializable {
    private long totalActiveStores;
    private long totalStores;
    private long totalCustomers;
    private long totalStoreOwners;
    private long totalDeliveryPartners;
    private long totalProducts;
    private long ordersToday;
    private long pendingOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunds;
    private long inventoryAlerts;

    public DashboardStats() {
    }

    public long getTotalActiveStores() {
        return totalActiveStores;
    }

    public void setTotalActiveStores(long totalActiveStores) {
        this.totalActiveStores = totalActiveStores;
    }

    public long getTotalStores() {
        return totalStores;
    }

    public void setTotalStores(long totalStores) {
        this.totalStores = totalStores;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalStoreOwners() {
        return totalStoreOwners;
    }

    public void setTotalStoreOwners(long totalStoreOwners) {
        this.totalStoreOwners = totalStoreOwners;
    }

    public long getTotalDeliveryPartners() {
        return totalDeliveryPartners;
    }

    public void setTotalDeliveryPartners(long totalDeliveryPartners) {
        this.totalDeliveryPartners = totalDeliveryPartners;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getOrdersToday() {
        return ordersToday;
    }

    public void setOrdersToday(long ordersToday) {
        this.ordersToday = ordersToday;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(long deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalRefunds() {
        return totalRefunds != null ? totalRefunds : BigDecimal.ZERO;
    }

    public void setTotalRefunds(BigDecimal totalRefunds) {
        this.totalRefunds = totalRefunds;
    }

    public long getInventoryAlerts() {
        return inventoryAlerts;
    }

    public void setInventoryAlerts(long inventoryAlerts) {
        this.inventoryAlerts = inventoryAlerts;
    }

    // Backward compatibility helpers
    public BigDecimal getTotalSales() {
        return getTotalRevenue();
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalRevenue = totalSales;
    }

    public long getTotalOrders() {
        return ordersToday + pendingOrders + deliveredOrders + cancelledOrders;
    }

    public void setTotalOrders(long totalOrders) {
    }
}
