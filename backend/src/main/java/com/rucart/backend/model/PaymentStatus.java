package com.rucart.backend.model;

public enum PaymentStatus {
    INITIATED,
    PENDING,
    PENDING_VERIFICATION,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUND_PENDING,
    PARTIALLY_REFUNDED,
    REFUNDED
}
