package com.rucart.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class InventoryValidationRequest {

    @NotNull(message = "Store ID is required")
    private Long storeId;

    @NotEmpty(message = "Items list cannot be empty")
    private List<OrderItemRequest> items;

    public InventoryValidationRequest() {
    }

    public InventoryValidationRequest(Long storeId, List<OrderItemRequest> items) {
        this.storeId = storeId;
        this.items = items;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
