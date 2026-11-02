package com.rucart.app.model;

import java.io.Serializable;

public class RegisterRequest implements Serializable {
    private String name;
    private String phone;
    private String email;
    private String password;
    private Role role = Role.CUSTOMER;
    private String address;

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String phone, String email, String password, Role role, String address) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : Role.CUSTOMER;
        this.address = address;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
