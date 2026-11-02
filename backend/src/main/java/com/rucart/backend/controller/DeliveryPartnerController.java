package com.rucart.backend.controller;

import com.rucart.backend.dto.ApiResponse;
import com.rucart.backend.dto.DeliveryDashboardDto;
import com.rucart.backend.dto.DeliveryIssueReportRequest;
import com.rucart.backend.dto.DeliveryPartnerDto;
import com.rucart.backend.model.DeliveryPartner;
import com.rucart.backend.model.DeliveryPartnerStatus;
import com.rucart.backend.model.Order;
import com.rucart.backend.model.OrderStatus;
import com.rucart.backend.security.CustomUserDetails;
import com.rucart.backend.service.DeliveryPartnerService;
import com.rucart.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/delivery")
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;
    private final OrderService orderService;

    public DeliveryPartnerController(DeliveryPartnerService deliveryPartnerService, OrderService orderService) {
        this.deliveryPartnerService = deliveryPartnerService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryDashboardDto>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DeliveryDashboardDto dashboard = deliveryPartnerService.getDeliveryDashboard(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(dashboard));
    }

    @GetMapping("/partner/me")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryPartner>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DeliveryPartner partner = deliveryPartnerService.getByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(partner));
    }

    @PostMapping("/partner/profile")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryPartner>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DeliveryPartnerDto dto) {
        DeliveryPartner partner = deliveryPartnerService.registerOrUpdatePartner(userDetails.getId(), dto);
        return ResponseEntity.ok(ApiResponse.ok("Delivery partner profile saved", partner));
    }

    @PatchMapping("/partner/availability")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryPartner>> setAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam boolean available) {
        DeliveryPartner partner = deliveryPartnerService.setAvailability(userDetails.getId(), available);
        return ResponseEntity.ok(ApiResponse.ok("Availability updated", partner));
    }

    @PatchMapping("/status")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryPartner>> setStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam DeliveryPartnerStatus status) {
        DeliveryPartner partner = deliveryPartnerService.setPartnerStatus(userDetails.getId(), status);
        return ResponseEntity.ok(ApiResponse.ok("Status updated to " + status.name(), partner));
    }

    @GetMapping("/available-orders")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Order>>> getAvailableOrders() {
        List<Order> orders = orderService.getAvailableOrdersForDelivery();
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Order>>> getMyActiveOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Order> orders = orderService.getActiveDeliveriesForPartner(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Order>> getOrderDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        // Verify partner access if already assigned to a partner
        if (order.getDeliveryPartner() != null && !order.getDeliveryPartner().getId().equals(userDetails.getId())) {
            // Check if admin
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new SecurityException("Unauthorized: This order is assigned to another delivery partner");
            }
        }
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    @PostMapping("/orders/{orderId}/claim")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Order>> claimOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        Order order = orderService.assignDeliveryPartner(orderId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("Order #" + order.getOrderNumber() + " claimed successfully", order));
    }

    @PostMapping("/orders/{orderId}/pickup")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Order>> confirmPickup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        Order order = orderService.confirmPickup(orderId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("Order #" + order.getOrderNumber() + " pickup confirmed", order));
    }

    @PostMapping("/orders/{orderId}/out-for-delivery")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Order>> startDelivery(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        Order order = orderService.startDelivery(orderId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("Order #" + order.getOrderNumber() + " is now out for delivery", order));
    }

    @PostMapping("/orders/{orderId}/complete")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Order>> completeDelivery(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        Order order = orderService.completeDelivery(orderId, userDetails.getId(), notes);
        return ResponseEntity.ok(ApiResponse.ok("Order #" + order.getOrderNumber() + " marked as DELIVERED", order));
    }

    @PostMapping("/orders/{orderId}/report-issue")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Order>> reportIssue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            @Valid @RequestBody DeliveryIssueReportRequest request) {
        Order order = orderService.reportDeliveryIssue(orderId, userDetails.getId(), request.getIssueType(), request.getDetails());
        return ResponseEntity.ok(ApiResponse.ok("Delivery issue recorded for order #" + order.getOrderNumber(), order));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Order>>> getDeliveryHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "all") String period) {
        List<Order> history = orderService.getDeliveryHistoryForPartner(userDetails.getId(), period);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @GetMapping("/available-partners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DeliveryPartner>>> getAvailablePartners() {
        return ResponseEntity.ok(ApiResponse.ok(deliveryPartnerService.getAvailablePartners()));
    }
}
