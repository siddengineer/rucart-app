package com.rucart.app.model;

import java.io.Serializable;

public class OrderStatusUpdateRequest implements Serializable {
    private String status;
    private Long deliveryPartnerId;
    private String deliveryOtp;

    public OrderStatusUpdateRequest() {
    }

    public OrderStatusUpdateRequest(String status, Long deliveryPartnerId) {
        this.status = status;
        this.deliveryPartnerId = deliveryPartnerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDeliveryPartnerId() {
        return deliveryPartnerId;
    }

    public void setDeliveryPartnerId(Long deliveryPartnerId) {
        this.deliveryPartnerId = deliveryPartnerId;
    }

    public String getDeliveryOtp() {
        return deliveryOtp;
    }

    public void setDeliveryOtp(String deliveryOtp) {
        this.deliveryOtp = deliveryOtp;
    }
}
