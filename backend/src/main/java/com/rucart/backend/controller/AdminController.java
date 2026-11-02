package com.rucart.backend.controller;

import com.rucart.backend.dto.*;
import com.rucart.backend.model.*;
import com.rucart.backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    private String getActor(Principal principal) {
        return principal != null ? principal.getName() : "ADMIN_USER";
    }

    // =========================================================================
    // 1. DASHBOARD & OPERATIONAL REPORTS
    // =========================================================================

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats()));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<OperationalReportDto>> getOperationalReports(
            @RequestParam(required = false, defaultValue = "week") String period) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getOperationalReports(period)));
    }

    // =========================================================================
    // 2. STORE MANAGEMENT
    // =========================================================================

    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<List<Store>>> getAllStores(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllStores(search)));
    }

    @PostMapping("/stores")
    public ResponseEntity<ApiResponse<Store>> createStore(
            @Valid @RequestBody StoreAdminRequest request,
            Principal principal) {
        Store store = adminService.createStore(request, getActor(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Store created successfully", store));
    }

    @PutMapping("/stores/{id}")
    public ResponseEntity<ApiResponse<Store>> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody StoreAdminRequest request,
            Principal principal) {
        Store store = adminService.updateStore(id, request, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Store updated successfully", store));
    }

    @PatchMapping("/stores/{id}/status")
    public ResponseEntity<ApiResponse<Store>> toggleStoreStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean open,
            Principal principal) {
        Store store = adminService.toggleStoreStatus(id, active, open, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Store status updated successfully", store));
    }

    // =========================================================================
    // 3. USER MANAGEMENT (CUSTOMER, STORE_OWNER, DELIVERY_PARTNER)
    // =========================================================================

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getUsersByRole(role, search)));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<User>> toggleUserStatus(
            @PathVariable Long id,
            @RequestBody UserStatusUpdateRequest request,
            Principal principal) {
        User user = adminService.toggleUserStatus(id, request, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("User account status updated", user));
    }

    // =========================================================================
    // 4. PRODUCT & CATEGORY MANAGEMENT
    // =========================================================================

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllProducts(categoryId, search)));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            Principal principal) {
        Product product = adminService.createProduct(request, getActor(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Product created successfully", product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            Principal principal) {
        Product product = adminService.updateProduct(id, request, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", product));
    }

    @PatchMapping("/products/{id}/status")
    public ResponseEntity<ApiResponse<Product>> toggleProductAvailability(
            @PathVariable Long id,
            @RequestParam boolean available,
            Principal principal) {
        Product product = adminService.toggleProductAvailability(id, available, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Product availability updated", product));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllCategories()));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @Valid @RequestBody CategoryRequest request,
            Principal principal) {
        Category category = adminService.createCategory(request, getActor(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Category created successfully", category));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            Principal principal) {
        Category category = adminService.updateCategory(id, request, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Category updated successfully", category));
    }

    // =========================================================================
    // 5. INVENTORY INSPECTION
    // =========================================================================

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<StoreInventory>>> getStoreInventories(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Boolean lowStockOnly) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getStoreInventories(storeId, lowStockOnly)));
    }

    // =========================================================================
    // 6. ORDER INSPECTION & STATUS OVERRIDE
    // =========================================================================

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<Order>>> searchOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.searchOrders(status, storeId, search, startDate, endDate)));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getOrderDetails(id)));
    }

    @PatchMapping("/orders/{id}/status-override")
    public ResponseEntity<ApiResponse<Order>> overrideOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @RequestParam(required = false, defaultValue = "Administrative override") String reason,
            Principal principal) {
        Order order = adminService.overrideOrderStatus(id, status, reason, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Order status updated by admin", order));
    }

    // =========================================================================
    // 7. PAYMENTS & REFUNDS
    // =========================================================================

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllPayments()));
    }

    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<List<Payment>>> getAllRefunds() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllRefunds()));
    }

    @PostMapping("/refunds/{paymentId}")
    public ResponseEntity<ApiResponse<Payment>> processRefund(
            @PathVariable Long paymentId,
            @Valid @RequestBody RefundProcessRequest request,
            Principal principal) {
        Payment payment = adminService.processRefund(paymentId, request, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Refund processed successfully", payment));
    }

    // =========================================================================
    // 8. AUDIT LOGS (READ-ONLY)
    // =========================================================================

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAuditLogs(targetType, action, actor)));
    }
}
