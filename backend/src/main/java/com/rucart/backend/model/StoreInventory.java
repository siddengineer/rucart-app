package com.rucart.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_inventories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"store_id", "product_id"})
})
public class StoreInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity = 50;

    @Column(nullable = false)
    private Integer lowStockThreshold = 10;

    @Column(nullable = false)
    private boolean available = true;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public StoreInventory() {
    }

    public StoreInventory(Store store, Product product, BigDecimal price, Integer stockQuantity, Integer lowStockThreshold) {
        this.store = store;
        this.product = product;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.lowStockThreshold = lowStockThreshold;
        this.available = stockQuantity != null && stockQuantity > 0;
        this.updatedAt = LocalDateTime.now();
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
        this.available = stockQuantity != null && stockQuantity > 0;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getQuantity() {
        return stockQuantity;
    }

    public void setQuantity(Integer quantity) {
        setStockQuantity(quantity);
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isLowStock() {
        return stockQuantity != null && lowStockThreshold != null && stockQuantity <= lowStockThreshold;
    }
}
