package com.rucart.backend.util;

public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final String productName;
    private final int availableStock;
    private final int requestedStock;

    public InsufficientStockException(Long productId, String productName, int availableStock, int requestedStock) {
        super(String.format("Insufficient stock for product '%s' (ID: %d). Available: %d, Requested: %d",
                productName, productId, availableStock, requestedStock));
        this.productId = productId;
        this.productName = productName;
        this.availableStock = availableStock;
        this.requestedStock = requestedStock;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getRequestedStock() {
        return requestedStock;
    }
}
