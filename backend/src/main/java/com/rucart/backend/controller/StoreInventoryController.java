package com.rucart.backend.controller;

import com.rucart.backend.dto.*;
import com.rucart.backend.model.StoreInventory;
import com.rucart.backend.service.StoreInventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class StoreInventoryController {

    private final StoreInventoryService inventoryService;

    public StoreInventoryController(StoreInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<StoreInventory>>> getStoreInventory(
            @PathVariable Long storeId,
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly) {
        List<StoreInventory> inventory = availableOnly
                ? inventoryService.getAvailableStoreInventory(storeId)
                : inventoryService.getStoreInventory(storeId);
        return ResponseEntity.ok(ApiResponse.ok(inventory));
    }

    @PostMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StoreInventory>> updateStoreInventory(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreInventoryRequest request) {
        StoreInventory inventory = inventoryService.setInventoryItem(storeId, request);
        return ResponseEntity.ok(ApiResponse.ok("Store inventory updated", inventory));
    }

    @PostMapping("/store/{storeId}/adjust")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StoreInventory>> adjustStockManually(
            @PathVariable Long storeId,
            @Valid @RequestBody StockAdjustmentRequest request) {
        StoreInventory inventory = inventoryService.adjustStockManually(storeId, request, null);
        return ResponseEntity.ok(ApiResponse.ok("Stock quantity adjusted successfully", inventory));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<InventoryValidationResponse>> validateInventory(
            @Valid @RequestBody InventoryValidationRequest request) {
        InventoryValidationResponse response = inventoryService.validateInventory(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/store/{storeId}/low-stock")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<LowStockAlertDto>>> getStoreLowStockAlerts(
            @PathVariable Long storeId) {
        List<LowStockAlertDto> alerts = inventoryService.getLowStockAlerts(storeId);
        return ResponseEntity.ok(ApiResponse.ok(alerts));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LowStockAlertDto>>> getAllLowStockAlerts() {
        List<LowStockAlertDto> alerts = inventoryService.getLowStockAlerts(null);
        return ResponseEntity.ok(ApiResponse.ok(alerts));
    }

    @GetMapping("/store/{storeId}/history")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<StockAdjustmentHistoryDto>>> getStoreStockHistory(
            @PathVariable Long storeId,
            @RequestParam(required = false) Long productId) {
        List<StockAdjustmentHistoryDto> history = inventoryService.getStockAdjustmentHistory(storeId, productId);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StockAdjustmentHistoryDto>>> getAllStockHistory(
            @RequestParam(required = false) Long productId) {
        List<StockAdjustmentHistoryDto> history = inventoryService.getStockAdjustmentHistory(null, productId);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}
