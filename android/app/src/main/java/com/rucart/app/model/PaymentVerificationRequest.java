package com.rucart.app.model;

import java.io.Serializable;

public class PaymentVerificationRequest implements Serializable {
    private Long orderId;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String gatewaySignature;
    private String paymentMethod;
    private String idempotencyKey;

    public PaymentVerificationRequest() {
    }

    public PaymentVerificationRequest(Long orderId, String gatewayOrderId, String gatewayPaymentId, String gatewaySignature) {
        this.orderId = orderId;
        this.gatewayOrderId = gatewayOrderId;
        this.gatewayPaymentId = gatewayPaymentId;
        this.gatewaySignature = gatewaySignature;
        this.paymentMethod = "ONLINE";
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getGatewayOrderId() {
        return gatewayOrderId;
    }

    public void setGatewayOrderId(String gatewayOrderId) {
        this.gatewayOrderId = gatewayOrderId;
    }

    public String getGatewayPaymentId() {
        return gatewayPaymentId;
    }

    public void setGatewayPaymentId(String gatewayPaymentId) {
        this.gatewayPaymentId = gatewayPaymentId;
    }

    public String getGatewaySignature() {
        return gatewaySignature;
    }

    public void setGatewaySignature(String gatewaySignature) {
        this.gatewaySignature = gatewaySignature;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
