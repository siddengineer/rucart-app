package com.rucart.backend.model;

public enum PaymentStatus {
    INITIATED,
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUND_PENDING,
    PARTIALLY_REFUNDED,
    REFUNDED
}
