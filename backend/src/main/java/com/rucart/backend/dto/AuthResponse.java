package com.rucart.backend.dto;

import com.rucart.backend.model.Role;
import com.rucart.backend.model.User;

public class AuthResponse {
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private Role role;
    private String token;
    private String tokenType = "Bearer";
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(User user, String token, String message) {
        this.userId = user.getId();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.token = token;
        this.tokenType = "Bearer";
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
