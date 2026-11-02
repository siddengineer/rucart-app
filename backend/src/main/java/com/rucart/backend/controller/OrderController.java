package com.rucart.backend.controller;

import com.rucart.backend.dto.ApiResponse;
import com.rucart.backend.dto.OrderCreateRequest;
import com.rucart.backend.dto.OrderStatusUpdateRequest;
import com.rucart.backend.model.Order;
import com.rucart.backend.model.OrderStatus;
import com.rucart.backend.security.CustomUserDetails;
import com.rucart.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request) {
        if (request.getCustomerId() == null && userDetails != null) {
            request.setCustomerId(userDetails.getId());
        }
        Order order = orderService.createOrder(request);
        return ResponseEntity.ok(ApiResponse.ok("Order placed successfully", order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderById(id)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Order>>> getCustomerOrders(
            @PathVariable Long customerId,
            @RequestParam(required = false) OrderStatus status) {
        List<Order> orders = orderService.getCustomerOrdersFiltered(customerId, status);
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<Order>>> getStoreOrders(
            @PathVariable Long storeId,
            @RequestParam(required = false) OrderStatus status) {
        List<Order> orders = orderService.getStoreOrdersFiltered(storeId, status);
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/delivery/available")
    public ResponseEntity<ApiResponse<List<Order>>> getAvailableDeliveries() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAvailableOrdersForDelivery()));
    }

    @GetMapping("/delivery/partner/{partnerId}")
    public ResponseEntity<ApiResponse<List<Order>>> getDeliveryPartnerOrders(@PathVariable Long partnerId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrdersByDeliveryPartner(partnerId)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateRequest request) {
        Order updated = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated", updated));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Customer requested cancellation") String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        Order cancelled = orderService.cancelOrder(id, reason, userId);
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled and inventory restored", cancelled));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<Order>> acceptOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long storeOwnerId = userDetails != null ? userDetails.getId() : null;
        Order accepted = orderService.acceptOrder(id, storeOwnerId);
        return ResponseEntity.ok(ApiResponse.ok("Order accepted by store", accepted));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Order>> rejectOrder(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Store unable to fulfill") String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long storeOwnerId = userDetails != null ? userDetails.getId() : null;
        Order rejected = orderService.rejectOrder(id, reason, storeOwnerId);
        return ResponseEntity.ok(ApiResponse.ok("Order rejected and stock released", rejected));
    }
}
