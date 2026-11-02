package com.rucart.app.model;

import java.io.Serializable;
import java.util.List;

public class InventoryValidationRequest implements Serializable {
    private Long storeId;
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
