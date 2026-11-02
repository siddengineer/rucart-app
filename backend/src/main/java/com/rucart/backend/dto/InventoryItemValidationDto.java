package com.rucart.backend.dto;

import java.math.BigDecimal;

public class InventoryItemValidationDto {

    private Long productId;
    private String productName;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private Boolean available;
    private BigDecimal price;
    private String statusMessage;

    public InventoryItemValidationDto() {
    }

    public InventoryItemValidationDto(Long productId, String productName, Integer requestedQuantity,
                                      Integer availableQuantity, Boolean available, BigDecimal price, String statusMessage) {
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
        this.available = available;
        this.price = price;
        this.statusMessage = statusMessage;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
