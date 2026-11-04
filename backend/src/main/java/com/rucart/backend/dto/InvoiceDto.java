package com.rucart.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceDto {

    private String invoiceNumber;
    private Long orderId;
    private String orderNumber;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String storeName;
    private String storeAddress;
    private List<InvoiceItemDto> items;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal grandTotal;
    private String paymentMethodDisplay; // "Cash on Delivery (COD)" or "UPI QR Payment"
    private String paymentMethod;
    private String paymentStatusDisplay; // "Paid", "Pending", "Pending Verification"
    private String paymentStatus;
    private LocalDateTime orderDate;
    private LocalDateTime deliveredAt;

    public static class InvoiceItemDto {
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public InvoiceItemDto() {}

        public InvoiceItemDto(String productName, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getStoreAddress() { return storeAddress; }
    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }
    public List<InvoiceItemDto> getItems() { return items; }
    public void setItems(List<InvoiceItemDto> items) { this.items = items; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    public String getPaymentMethodDisplay() { return paymentMethodDisplay; }
    public void setPaymentMethodDisplay(String paymentMethodDisplay) { this.paymentMethodDisplay = paymentMethodDisplay; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatusDisplay() { return paymentStatusDisplay; }
    public void setPaymentStatusDisplay(String paymentStatusDisplay) { this.paymentStatusDisplay = paymentStatusDisplay; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}
