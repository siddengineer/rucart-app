package com.rucart.app.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDto implements Serializable {
    private Long orderId;
    private String orderNumber;
    private String invoiceNumber;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String storeName;
    private String storeAddress;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal grandTotal;
    private String paymentMethod;
    private String paymentMethodDisplay;
    private String paymentStatus;
    private String paymentStatusDisplay;
    private String orderDate;
    private String deliveredAt;
    private List<InvoiceItemDto> items = new ArrayList<>();

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

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

    public BigDecimal getSubtotal() { return subtotal != null ? subtotal : BigDecimal.ZERO; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDeliveryFee() { return deliveryFee != null ? deliveryFee : BigDecimal.ZERO; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getGrandTotal() { return grandTotal != null ? grandTotal : BigDecimal.ZERO; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentMethodDisplay() { return paymentMethodDisplay; }
    public void setPaymentMethodDisplay(String paymentMethodDisplay) { this.paymentMethodDisplay = paymentMethodDisplay; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentStatusDisplay() { return paymentStatusDisplay; }
    public void setPaymentStatusDisplay(String paymentStatusDisplay) { this.paymentStatusDisplay = paymentStatusDisplay; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(String deliveredAt) { this.deliveredAt = deliveredAt; }

    public List<InvoiceItemDto> getItems() { return items; }
    public void setItems(List<InvoiceItemDto> items) { this.items = items; }

    public static class InvoiceItemDto implements Serializable {
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

        public BigDecimal getUnitPrice() { return unitPrice != null ? unitPrice : BigDecimal.ZERO; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public BigDecimal getTotalPrice() { return totalPrice != null ? totalPrice : BigDecimal.ZERO; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    }
}
