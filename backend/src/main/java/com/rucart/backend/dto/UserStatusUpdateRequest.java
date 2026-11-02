package com.rucart.backend.dto;

public class UserStatusUpdateRequest {
    private boolean active;
    private String reason;

    public UserStatusUpdateRequest() {
    }

    public UserStatusUpdateRequest(boolean active, String reason) {
        this.active = active;
        this.reason = reason;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
