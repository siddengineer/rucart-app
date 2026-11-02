package com.rucart.backend.model;

public enum StockAdjustmentType {
    INITIAL_STOCK,
    MANUAL_ADJUSTMENT,
    ORDER_RESERVATION,
    ORDER_CANCELLATION_RESTOCK,
    PURCHASE_RECEIPT,
    AUDIT_CORRECTION
}
