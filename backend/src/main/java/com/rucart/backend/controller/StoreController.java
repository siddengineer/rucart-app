package com.rucart.backend.controller;

import com.rucart.backend.dto.ApiResponse;
import com.rucart.backend.dto.StoreAssignmentRequest;
import com.rucart.backend.dto.StoreAssignmentResult;
import com.rucart.backend.dto.StoreOrderStatsDto;
import com.rucart.backend.dto.StoreStatusUpdateRequest;
import com.rucart.backend.model.Store;
import com.rucart.backend.service.StoreAssignmentService;
import com.rucart.backend.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;
    private final StoreAssignmentService assignmentService;

    public StoreController(StoreService storeService, StoreAssignmentService assignmentService) {
        this.storeService = storeService;
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Store>>> getStores() {
        return ResponseEntity.ok(ApiResponse.ok(storeService.getOpenStores()));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Store>>> getAllStores() {
        return ResponseEntity.ok(ApiResponse.ok(storeService.getAllStores()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Store>> getStoreById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(storeService.getStoreById(id)));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<StoreOrderStatsDto>> getStoreStats(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(storeService.getStoreOrderStats(id)));
    }

    @GetMapping("/service-areas")
    public ResponseEntity<ApiResponse<List<String>>> getServiceAreas() {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getSupportedServiceAreas()));
    }

    @GetMapping("/by-area")
    public ResponseEntity<ApiResponse<List<Store>>> getStoresByArea(@RequestParam String area) {
        return ResponseEntity.ok(ApiResponse.ok(storeService.getStoresByServiceArea(area)));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<StoreAssignmentResult>> previewStoreAssignment(
            @RequestBody StoreAssignmentRequest request) {
        StoreAssignmentResult result = assignmentService.assignStoreForOrder(request.getDeliveryArea(), request.getItems());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Store>> updateStoreStatus(
            @PathVariable Long id,
            @RequestBody StoreStatusUpdateRequest request) {
        Store updated = storeService.updateStoreStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Store status updated successfully", updated));
    }
}
