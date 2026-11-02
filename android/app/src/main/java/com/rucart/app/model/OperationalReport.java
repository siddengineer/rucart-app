package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperationalReport implements Serializable {
    private String period;
    private long totalOrders;
    private long completedOrders;
    private long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunds;
    private long activeStoresCount;
    private long totalProductsCount;
    private long lowStockAlertsCount;
    private List<StorePerformance> storePerformances = new ArrayList<>();
    private Map<String, Long> paymentMethodBreakdown = new HashMap<>();

    public OperationalReport() {
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(long completedOrders) {
        this.completedOrders = completedOrders;
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

    public long getActiveStoresCount() {
        return activeStoresCount;
    }

    public void setActiveStoresCount(long activeStoresCount) {
        this.activeStoresCount = activeStoresCount;
    }

    public long getTotalProductsCount() {
        return totalProductsCount;
    }

    public void setTotalProductsCount(long totalProductsCount) {
        this.totalProductsCount = totalProductsCount;
    }

    public long getLowStockAlertsCount() {
        return lowStockAlertsCount;
    }

    public void setLowStockAlertsCount(long lowStockAlertsCount) {
        this.lowStockAlertsCount = lowStockAlertsCount;
    }

    public List<StorePerformance> getStorePerformances() {
        return storePerformances;
    }

    public void setStorePerformances(List<StorePerformance> storePerformances) {
        this.storePerformances = storePerformances;
    }

    public Map<String, Long> getPaymentMethodBreakdown() {
        return paymentMethodBreakdown;
    }

    public void setPaymentMethodBreakdown(Map<String, Long> paymentMethodBreakdown) {
        this.paymentMethodBreakdown = paymentMethodBreakdown;
    }
}
