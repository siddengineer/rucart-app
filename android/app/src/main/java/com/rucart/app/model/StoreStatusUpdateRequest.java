package com.rucart.app.model;

import java.io.Serializable;

public class StoreStatusUpdateRequest implements Serializable {
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
