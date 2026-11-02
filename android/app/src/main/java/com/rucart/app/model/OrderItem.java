package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderItem implements Serializable {

    private Long id;
    private Product product;
    private String productName;
    private String unit;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public OrderItem() {
    }

    // Product name
    public String getProductName() {
        return productName != null
                ? productName
                : (product != null && product.getName() != null
                ? product.getName()
                : "Item");
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    // Unit
    public String getUnit() {
        return unit != null
                ? unit
                : (product != null && product.getUnit() != null
                ? product.getUnit()
                : "");
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    // ID
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Product
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    // Quantity
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // Unit price
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    // Total price
    public BigDecimal getTotalPrice() {
        if (totalPrice != null) {
            return totalPrice;
        }

        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }

        return BigDecimal.ZERO;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Compatibility method for existing code using getSubtotal()
    public BigDecimal getSubtotal() {
        return getTotalPrice();
    }
}
