package com.rucart.app.model;

import java.io.Serializable;
import java.util.List;

public class StoreAssignmentResult implements Serializable {
    private Store assignedStore;
    private String deliveryArea;
    private boolean canFulfill;
    private String message;
    private int candidateStoresEvaluated;
    private List<String> unfulfillableItems;

    public StoreAssignmentResult() {
    }

    public Store getAssignedStore() {
        return assignedStore;
    }

    public void setAssignedStore(Store assignedStore) {
        this.assignedStore = assignedStore;
    }

    public String getDeliveryArea() {
        return deliveryArea;
    }

    public void setDeliveryArea(String deliveryArea) {
        this.deliveryArea = deliveryArea;
    }

    public boolean isCanFulfill() {
        return canFulfill;
    }

    public void setCanFulfill(boolean canFulfill) {
        this.canFulfill = canFulfill;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCandidateStoresEvaluated() {
        return candidateStoresEvaluated;
    }

    public void setCandidateStoresEvaluated(int candidateStoresEvaluated) {
        this.candidateStoresEvaluated = candidateStoresEvaluated;
    }

    public List<String> getUnfulfillableItems() {
        return unfulfillableItems;
    }

    public void setUnfulfillableItems(List<String> unfulfillableItems) {
        this.unfulfillableItems = unfulfillableItems;
    }
}
