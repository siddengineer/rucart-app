package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class Order implements Serializable {
    private Long id;
    private String orderNumber;
    private User customer;
    private Store store;
    private User deliveryPartner;
    private String status;
    private BigDecimal itemTotal;
    private BigDecimal deliveryFee;
    private BigDecimal discount;
    private BigDecimal grandTotal;
    private String deliveryAddress;
    private String paymentMethod;
    private String paymentStatus;
    private String deliveryOtp;
    private String specialInstructions;
    private String customerName;
    private String customerPhone;
    private String storeName;
    private String cancellationReason;
    private String cancelledAt;
    private String idempotencyKey;
    private String assignedAt;
    private String pickedUpAt;
    private String deliveredAt;
    private String deliveryNotes;
    private String deliveryIssueReason;
    private String deliveryIssueDetails;
    private String deliveryIssueAt;
    private List<OrderItem> items;
    private Payment payment;
    private String createdAt;

    public Order() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public User getDeliveryPartner() {
        return deliveryPartner;
    }

    public void setDeliveryPartner(User deliveryPartner) {
        this.deliveryPartner = deliveryPartner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(BigDecimal itemTotal) {
        this.itemTotal = itemTotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getDeliveryOtp() {
        return deliveryOtp;
    }

    public void setDeliveryOtp(String deliveryOtp) {
        this.deliveryOtp = deliveryOtp;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getOtp() {
        return deliveryOtp;
    }

    public void setOtp(String otp) {
        this.deliveryOtp = otp;
    }

    public BigDecimal getTotalAmount() {
        return grandTotal;
    }

    public void setTotalAmount(BigDecimal amount) {
        this.grandTotal = amount;
    }

    public String getCustomerName() {
        return customerName != null ? customerName : (customer != null ? customer.getName() : "Customer");
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone != null ? customerPhone : (customer != null ? customer.getPhone() : "");
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(String cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(String assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getPickedUpAt() {
        return pickedUpAt;
    }

    public void setPickedUpAt(String pickedUpAt) {
        this.pickedUpAt = pickedUpAt;
    }

    public String getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(String deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    public String getDeliveryIssueReason() {
        return deliveryIssueReason;
    }

    public void setDeliveryIssueReason(String deliveryIssueReason) {
        this.deliveryIssueReason = deliveryIssueReason;
    }

    public String getDeliveryIssueDetails() {
        return deliveryIssueDetails;
    }

    public void setDeliveryIssueDetails(String deliveryIssueDetails) {
        this.deliveryIssueDetails = deliveryIssueDetails;
    }

    public String getDeliveryIssueAt() {
        return deliveryIssueAt;
    }

    public void setDeliveryIssueAt(String deliveryIssueAt) {
        this.deliveryIssueAt = deliveryIssueAt;
    }

    public String getStoreName() {
        if (storeName != null && !storeName.isEmpty()) return storeName;
        return store != null ? store.getName() : "RUCART Express Store, Shivajinagar";
    }
}
