package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class InventoryValidationResponse implements Serializable {

    private boolean valid;
    private String message;
    private List<ItemValidation> items;

    public InventoryValidationResponse() {
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ItemValidation> getItems() {
        return items;
    }

    public void setItems(List<ItemValidation> items) {
        this.items = items;
    }

    public static class ItemValidation implements Serializable {
        private Long productId;
        private String productName;
        private Integer requestedQuantity;
        private Integer availableQuantity;
        private Boolean available;
        private BigDecimal price;
        private String statusMessage;

        public ItemValidation() {
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
}
