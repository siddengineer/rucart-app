package com.rucart.backend.controller;

import com.rucart.backend.dto.*;
import com.rucart.backend.model.*;
import com.rucart.backend.service.AdminService;
import com.rucart.backend.service.OfferBannerService;
import com.rucart.backend.service.PaymentService;
import com.rucart.backend.service.RiderCommissionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final RiderCommissionService commissionService;
    private final OfferBannerService offerBannerService;
    private final PaymentService paymentService;

    public AdminController(AdminService adminService,
                           RiderCommissionService commissionService,
                           OfferBannerService offerBannerService,
                           PaymentService paymentService) {
        this.adminService = adminService;
        this.commissionService = commissionService;
        this.offerBannerService = offerBannerService;
        this.paymentService = paymentService;
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

    // =========================================================================
    // 9. RIDER MANAGEMENT
    // =========================================================================

    @GetMapping("/riders")
    public ResponseEntity<ApiResponse<List<RiderResponse>>> getAllRiders(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllRiders(search)));
    }

    @PostMapping("/riders")
    public ResponseEntity<ApiResponse<RiderResponse>> registerRider(
            @Valid @RequestBody RiderRegisterRequest request,
            Principal principal) {
        RiderResponse resp = adminService.registerRider(request, getActor(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Rider registered successfully", resp));
    }

    @PostMapping("/riders/{id}/reset-password")
    public ResponseEntity<ApiResponse<String>> resetRiderPassword(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Principal principal) {
        String newPass = body != null ? body.get("newPassword") : null;
        String pass = adminService.resetRiderPassword(id, newPass, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Rider password reset successfully", pass));
    }

    @PatchMapping("/riders/{id}/status")
    public ResponseEntity<ApiResponse<RiderResponse>> updateRiderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Principal principal) {
        RiderResponse resp = adminService.updateRiderStatus(id, status, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Rider status updated", resp));
    }

    @GetMapping("/riders/{id}/orders")
    public ResponseEntity<ApiResponse<List<Order>>> getRiderOrders(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getRiderDeliveredOrders(id)));
    }

    // =========================================================================
    // 10. STORE OWNER MANAGEMENT
    // =========================================================================

    @GetMapping("/store-owners")
    public ResponseEntity<ApiResponse<List<StoreOwnerResponse>>> getAllStoreOwners(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllStoreOwners(search)));
    }

    @PostMapping("/store-owners")
    public ResponseEntity<ApiResponse<StoreOwnerResponse>> registerStoreOwner(
            @Valid @RequestBody StoreOwnerRegisterRequest request,
            Principal principal) {
        StoreOwnerResponse resp = adminService.registerStoreOwner(request, getActor(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Store owner registered successfully", resp));
    }

    @PostMapping("/store-owners/{id}/reset-password")
    public ResponseEntity<ApiResponse<String>> resetStoreOwnerPassword(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Principal principal) {
        String newPass = body != null ? body.get("newPassword") : null;
        String pass = adminService.resetStoreOwnerPassword(id, newPass, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Store owner password reset successfully", pass));
    }

    @PatchMapping("/store-owners/{id}/status")
    public ResponseEntity<ApiResponse<StoreOwnerResponse>> updateStoreOwnerStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            Principal principal) {
        StoreOwnerResponse resp = adminService.updateStoreOwnerStatus(id, active, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Store owner status updated", resp));
    }

    @GetMapping("/store-owners/{id}/orders")
    public ResponseEntity<ApiResponse<List<Order>>> getStoreOwnerOrders(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getStoreOwnerOrders(id)));
    }

    // =========================================================================
    // 11. CUSTOMER MANAGEMENT
    // =========================================================================

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<User>>> getAllCustomers(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getUsersByRole(Role.CUSTOMER, search)));
    }

    @GetMapping("/customers/{id}/orders")
    public ResponseEntity<ApiResponse<List<Order>>> getCustomerOrders(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.searchOrders(null, null, null, null, null).stream()
                .filter(o -> o.getCustomer() != null && o.getCustomer().getId().equals(id))
                .toList()));
    }

    // =========================================================================
    // 12. RIDER COMMISSION MANAGEMENT
    // =========================================================================

    @GetMapping("/commission")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCommissionConfig() {
        RiderCommissionDto current = commissionService.getCurrentCommission();
        List<RiderCommissionHistory> history = commissionService.getHistory();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("current", current, "history", history)));
    }

    @RequestMapping(value = "/commission", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<RiderCommissionDto>> updateCommission(
            @RequestBody Map<String, Object> body,
            Principal principal) {
        String typeStr = (String) body.get("commissionType");
        CommissionType type = "PERCENTAGE".equalsIgnoreCase(typeStr) ? CommissionType.PERCENTAGE : CommissionType.FIXED;
        Object rateObj = body.get("commissionRate");
        java.math.BigDecimal rate = new java.math.BigDecimal(rateObj != null ? rateObj.toString() : "35.00");
        String notes = (String) body.get("notes");

        RiderCommissionDto updated = commissionService.updateCommission(type, rate, notes, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Rider commission updated successfully", updated));
    }

    // =========================================================================
    // 13. INVOICE MANAGEMENT
    // =========================================================================

    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceDto>>> getAllInvoices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String paymentStatus) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllInvoices(search, paymentMethod, paymentStatus)));
    }

    @GetMapping("/invoices/{orderId}")
    public ResponseEntity<ApiResponse<InvoiceDto>> getInvoiceDetails(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getInvoiceByOrderId(orderId)));
    }

    @PostMapping("/invoices/{orderId}/confirm-payment")
    public ResponseEntity<ApiResponse<InvoiceDto>> confirmInvoicePayment(
            @PathVariable Long orderId,
            Principal principal) {
        InvoiceDto updated = adminService.confirmInvoicePayment(orderId, getActor(principal));
        return ResponseEntity.ok(ApiResponse.ok("Payment confirmed and marked as Paid", updated));
    }

    @GetMapping("/invoices/{orderId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(
            @PathVariable Long orderId,
            Principal principal) {
        com.rucart.backend.model.Payment payment = paymentService.getPaymentByOrderId(orderId);
        byte[] pdfBytes = paymentService.generateReceiptPdf(payment.getId(), null, true);
        InvoiceDto inv = adminService.getInvoiceByOrderId(orderId);
        String filename = "RUCART_Invoice_" + inv.getOrderNumber() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }

    // =========================================================================
    // 14. OFFERS & PROMOTIONAL BANNERS
    // =========================================================================

    @GetMapping("/offers")
    public ResponseEntity<ApiResponse<List<Offer>>> getAllOffers() {
        return ResponseEntity.ok(ApiResponse.ok(offerBannerService.getAllOffers()));
    }

    @PostMapping("/offers")
    public ResponseEntity<ApiResponse<Offer>> createOffer(@Valid @RequestBody Offer offer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Offer created successfully", offerBannerService.createOffer(offer)));
    }

    @PutMapping("/offers/{id}")
    public ResponseEntity<ApiResponse<Offer>> updateOffer(@PathVariable Long id, @Valid @RequestBody Offer offer) {
        return ResponseEntity.ok(ApiResponse.ok("Offer updated successfully", offerBannerService.updateOffer(id, offer)));
    }

    @DeleteMapping("/offers/{id}")
    public ResponseEntity<ApiResponse<String>> deleteOffer(@PathVariable Long id) {
        offerBannerService.deleteOffer(id);
        return ResponseEntity.ok(ApiResponse.ok("Offer deleted successfully", "DELETED"));
    }

    @GetMapping("/banners")
    public ResponseEntity<ApiResponse<List<PromotionalBanner>>> getAllBanners() {
        return ResponseEntity.ok(ApiResponse.ok(offerBannerService.getAllBanners()));
    }

    @PostMapping("/banners")
    public ResponseEntity<ApiResponse<PromotionalBanner>> createBanner(@Valid @RequestBody PromotionalBanner banner) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Banner created successfully", offerBannerService.createBanner(banner)));
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<ApiResponse<PromotionalBanner>> updateBanner(@PathVariable Long id, @Valid @RequestBody PromotionalBanner banner) {
        return ResponseEntity.ok(ApiResponse.ok("Banner updated successfully", offerBannerService.updateBanner(id, banner)));
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBanner(@PathVariable Long id) {
        offerBannerService.deleteBanner(id);
        return ResponseEntity.ok(ApiResponse.ok("Banner deleted successfully", "DELETED"));
    }
}
