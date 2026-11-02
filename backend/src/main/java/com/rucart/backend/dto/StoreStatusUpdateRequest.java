package com.rucart.backend.dto;

public class StoreStatusUpdateRequest {
    private Boolean open;
    private Boolean active;

    public StoreStatusUpdateRequest() {
    }

    public StoreStatusUpdateRequest(Boolean open, Boolean active) {
        this.open = open;
        this.active = active;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
