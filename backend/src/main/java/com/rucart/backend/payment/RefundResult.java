package com.rucart.backend.payment;

import java.math.BigDecimal;

public class RefundResult {
    private String refundId;
    private BigDecimal amount;
    private String status;
    private String message;
    private boolean successful;

    public RefundResult() {
    }

    public RefundResult(String refundId, BigDecimal amount, String status, String message, boolean successful) {
        this.refundId = refundId;
        this.amount = amount;
        this.status = status;
        this.message = message;
        this.successful = successful;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
