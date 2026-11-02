package com.rucart.backend.payment;

import java.math.BigDecimal;

public class PaymentOrderResult {
    private String gatewayOrderId;
    private long amountInPaise;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private boolean sandbox;
    private String receiptNumber;
    private String orderNumber;

    public PaymentOrderResult() {
    }

    public PaymentOrderResult(String gatewayOrderId, long amountInPaise, BigDecimal amount, String currency,
                              String keyId, boolean sandbox, String receiptNumber, String orderNumber) {
        this.gatewayOrderId = gatewayOrderId;
        this.amountInPaise = amountInPaise;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
        this.sandbox = sandbox;
        this.receiptNumber = receiptNumber;
        this.orderNumber = orderNumber;
    }

    public String getGatewayOrderId() {
        return gatewayOrderId;
    }

    public void setGatewayOrderId(String gatewayOrderId) {
        this.gatewayOrderId = gatewayOrderId;
    }

    public long getAmountInPaise() {
        return amountInPaise;
    }

    public void setAmountInPaise(long amountInPaise) {
        this.amountInPaise = amountInPaise;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
