package com.rucart.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class Msg91VerifyRequest {

    @NotBlank(message = "MSG91 access token is required")
    private String accessToken;

    private String phone;
    private String name;
    private String role;

    public Msg91VerifyRequest() {}

    public Msg91VerifyRequest(String accessToken, String phone, String name, String role) {
        this.accessToken = accessToken;
        this.phone = phone;
        this.name = name;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
