package com.rucart.backend.dto;

import com.rucart.backend.model.Store;
import java.util.ArrayList;
import java.util.List;

public class StoreAssignmentResult {
    private Store assignedStore;
    private String deliveryArea;
    private boolean canFulfill;
    private String message;
    private int candidateStoresEvaluated;
    private List<String> unfulfillableItems = new ArrayList<>();

    public StoreAssignmentResult() {
    }

    public StoreAssignmentResult(Store assignedStore, String deliveryArea, boolean canFulfill, String message) {
        this.assignedStore = assignedStore;
        this.deliveryArea = deliveryArea;
        this.canFulfill = canFulfill;
        this.message = message;
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
