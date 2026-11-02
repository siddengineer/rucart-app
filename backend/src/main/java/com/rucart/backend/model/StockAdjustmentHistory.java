package com.rucart.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_adjustment_histories")
public class StockAdjustmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer previousQuantity;

    @Column(nullable = false)
    private Integer newQuantity;

    @Column(nullable = false)
    private Integer quantityChange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StockAdjustmentType adjustmentType;

    @Column(length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "adjusted_by_user_id")
    private User adjustedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public StockAdjustmentHistory() {
    }

    public StockAdjustmentHistory(Store store, Product product, Integer previousQuantity, Integer newQuantity,
                                  StockAdjustmentType adjustmentType, String reason, User adjustedBy) {
        this.store = store;
        this.product = product;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.quantityChange = newQuantity - previousQuantity;
        this.adjustmentType = adjustmentType;
        this.reason = reason;
        this.adjustedBy = adjustedBy;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getPreviousQuantity() {
        return previousQuantity;
    }

    public void setPreviousQuantity(Integer previousQuantity) {
        this.previousQuantity = previousQuantity;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public StockAdjustmentType getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(StockAdjustmentType adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public User getAdjustedBy() {
        return adjustedBy;
    }

    public void setAdjustedBy(User adjustedBy) {
        this.adjustedBy = adjustedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
