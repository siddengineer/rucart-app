package com.rucart.app.model;

import java.io.Serializable;
import java.util.List;

public class StoreAssignmentRequest implements Serializable {
    private String deliveryArea;
    private List<OrderItemRequest> items;

    public StoreAssignmentRequest() {
    }

    public StoreAssignmentRequest(String deliveryArea, List<OrderItemRequest> items) {
        this.deliveryArea = deliveryArea;
        this.items = items;
    }

    public String getDeliveryArea() {
        return deliveryArea;
    }

    public void setDeliveryArea(String deliveryArea) {
        this.deliveryArea = deliveryArea;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
