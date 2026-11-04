package com.rucart.app.model;

import java.io.Serializable;

public class RiderRegisterRequest implements Serializable {
    private String name;
    private String phone;
    private String email;
    private String address;
    private String vehicleType;
    private String vehicleNumber;

    public RiderRegisterRequest() {}

    public RiderRegisterRequest(String name, String phone, String email, String address, String vehicleType, String vehicleNumber) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
}
