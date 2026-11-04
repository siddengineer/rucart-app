package com.rucart.app.model;

import java.io.Serializable;

public class StoreOwnerRegisterRequest implements Serializable {
    private String name;
    private String phone;
    private String email;
    private String address;
    private String storeName;
    private String storeArea;

    public StoreOwnerRegisterRequest() {}

    public StoreOwnerRegisterRequest(String name, String phone, String email, String address, String storeName, String storeArea) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.storeName = storeName;
        this.storeArea = storeArea;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreArea() { return storeArea; }
    public void setStoreArea(String storeArea) { this.storeArea = storeArea; }
}
