package com.rucart.app.model;

import java.io.Serializable;

public class CustomerAddress implements Serializable {
    private Long id;
    private String title = "Home";
    private String recipientName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    private String city = "Pune";
    private String state = "Maharashtra";
    private String postalCode = "411005";
    private Double latitude;
    private Double longitude;
    private boolean isDefault;

    public CustomerAddress() {
    }

    public CustomerAddress(Long id, String title, String addressLine1, String city, String postalCode, boolean isDefault) {
        this.id = id;
        this.title = title;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }

    public CustomerAddress(String title, String recipientName, String phoneNumber, String addressLine1, String addressLine2, String city, String postalCode, boolean isDefault) {
        this.title = title;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) sb.append(addressLine1);
        if (landmark != null && !landmark.isBlank()) sb.append(", Near ").append(landmark);
        if (city != null) sb.append(", ").append(city);
        if (postalCode != null) sb.append(" - ").append(postalCode);
        return sb.toString();
    }
}
