package com.rucart.backend.dto;

import com.rucart.backend.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentVerificationRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Gateway order ID is required")
    private String gatewayOrderId;

    @NotBlank(message = "Gateway payment ID is required")
    private String gatewayPaymentId;

    @NotBlank(message = "Gateway signature is required")
    private String gatewaySignature;

    private PaymentMethod paymentMethod = PaymentMethod.ONLINE;

    private String idempotencyKey;

    public PaymentVerificationRequest() {
    }

    public PaymentVerificationRequest(Long orderId, String gatewayOrderId, String gatewayPaymentId, String gatewaySignature) {
        this.orderId = orderId;
        this.gatewayOrderId = gatewayOrderId;
        this.gatewayPaymentId = gatewayPaymentId;
        this.gatewaySignature = gatewaySignature;
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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod != null ? paymentMethod : PaymentMethod.ONLINE;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
