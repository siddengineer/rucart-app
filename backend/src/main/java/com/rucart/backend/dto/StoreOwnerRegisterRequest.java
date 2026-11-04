package com.rucart.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class StoreOwnerRegisterRequest {

    @NotBlank(message = "Store owner name is required")
    private String name;

    @NotBlank(message = "Mobile number is required")
    private String phone;

    private String email;

    private String address;

    @NotBlank(message = "Store name is required")
    private String storeName;

    private String storeArea = "Shivajinagar, Pune";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreArea() {
        return storeArea;
    }

    public void setStoreArea(String storeArea) {
        this.storeArea = storeArea;
    }
}
