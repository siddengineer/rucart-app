package com.rucart.backend.dto;

import java.util.List;

public class InventoryValidationResponse {

    private boolean valid;
    private String message;
    private List<InventoryItemValidationDto> items;

    public InventoryValidationResponse() {
    }

    public InventoryValidationResponse(boolean valid, String message, List<InventoryItemValidationDto> items) {
        this.valid = valid;
        this.message = message;
        this.items = items;
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

    public List<InventoryItemValidationDto> getItems() {
        return items;
    }

    public void setItems(List<InventoryItemValidationDto> items) {
        this.items = items;
    }
}
