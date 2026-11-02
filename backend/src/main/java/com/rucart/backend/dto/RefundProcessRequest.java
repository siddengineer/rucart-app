package com.rucart.backend.dto;

import java.math.BigDecimal;

public class RefundProcessRequest {
    private BigDecimal amount;
    private String reason;

    public RefundProcessRequest() {
    }

    public RefundProcessRequest(BigDecimal amount, String reason) {
        this.amount = amount;
        this.reason = reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
