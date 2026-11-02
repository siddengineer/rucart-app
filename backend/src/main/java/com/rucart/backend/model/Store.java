package com.rucart.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city = "Pune";

    private String postalCode = "411005";

    private String phone;

    private String email;

    private Double rating = 4.5;

    @Column(nullable = false)
    private boolean open = true;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean approved = true;

    @Column(length = 500)
    private String serviceAreas = "Pune";

    private String contactPerson;

    private String imageUrl;

    private String openingTime = "07:00 AM";

    private String closingTime = "11:00 PM";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Store() {
    }

    public Store(String name, String description, String address, String city, String phone, User owner, String imageUrl) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.owner = owner;
        this.imageUrl = imageUrl;
        this.open = true;
        this.approved = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
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

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getServiceAreas() {
        return serviceAreas;
    }

    public void setServiceAreas(String serviceAreas) {
        this.serviceAreas = serviceAreas;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    /**
     * Store is operating if active, approved, and currently open.
     */
    public boolean isOperating() {
        return active && approved && open;
    }

    /**
     * Checks if this store specifically serves the specified locality or area.
     */
    public boolean servesArea(String area) {
        if (area == null || area.trim().isEmpty()) {
            return true;
        }
        String cleanArea = area.toLowerCase().trim();

        // 1. Check specific service localities first
        if (serviceAreas != null) {
            String[] areas = serviceAreas.split("[,;]");
            for (String a : areas) {
                String trimmed = a.trim().toLowerCase();
                if (!trimmed.isEmpty() && !trimmed.equals("pune") && !trimmed.equals("india")) {
                    if (cleanArea.contains(trimmed) || trimmed.contains(cleanArea)) {
                        return true;
                    }
                }
            }
        }

        // 2. Check store address locality
        if (address != null) {
            String[] addrParts = address.split("[,;]");
            for (String p : addrParts) {
                String trimmed = p.trim().toLowerCase();
                if (!trimmed.isEmpty() && !trimmed.equals("pune") && cleanArea.contains(trimmed)) {
                    return true;
                }
            }
        }

        // 3. Broader city match only if cleanArea is just the city
        if (city != null && (cleanArea.equals(city.toLowerCase().trim()) || cleanArea.equals("pune"))) {
            return true;
        }

        return false;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
