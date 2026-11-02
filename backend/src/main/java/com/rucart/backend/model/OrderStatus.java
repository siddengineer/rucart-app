package com.rucart.backend.model;

public enum OrderStatus {
    CART,
    PLACED,
    PAYMENT_PENDING,
    PAYMENT_CONFIRMED,
    ACCEPTED,
    PREPARING,
    PACKED,
    READY_FOR_PICKUP,
    ASSIGNED,
    PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    DELIVERY_FAILED,
    CANCELLED,
    REFUND_PENDING,
    REFUNDED
}
