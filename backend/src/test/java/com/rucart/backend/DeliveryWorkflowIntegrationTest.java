package com.rucart.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rucart.backend.dto.DeliveryIssueReportRequest;
import com.rucart.backend.dto.OrderItemRequest;
import com.rucart.backend.dto.OrderCreateRequest;
import com.rucart.backend.model.*;
import com.rucart.backend.repository.*;
import com.rucart.backend.security.CustomUserDetails;
import com.rucart.backend.security.JwtUtil;
import com.rucart.backend.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DeliveryWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    private User deliveryPartnerUser;
    private User customerUser;
    private User storeOwnerUser;

    private String deliveryPartnerToken;
    private String customerToken;
    private String storeOwnerToken;

    @BeforeEach
    public void setup() {
        customerUser = userRepository.findByPhone("9876543210").orElseThrow();
        storeOwnerUser = userRepository.findByPhone("9876543211").orElseThrow();
        deliveryPartnerUser = userRepository.findByPhone("9876543212").orElseThrow();

        deliveryPartnerToken = "Bearer " + jwtUtil.generateToken(
                new CustomUserDetails(deliveryPartnerUser), deliveryPartnerUser.getId(), deliveryPartnerUser.getRole());
        customerToken = "Bearer " + jwtUtil.generateToken(
                new CustomUserDetails(customerUser), customerUser.getId(), customerUser.getRole());
        storeOwnerToken = "Bearer " + jwtUtil.generateToken(
                new CustomUserDetails(storeOwnerUser), storeOwnerUser.getId(), storeOwnerUser.getRole());
    }

    private Order createTestOrderReadyForPickup() {
        Store store = storeRepository.findAll().stream().filter(Store::isOperating).findFirst().orElseThrow();
        Product product = productRepository.findAll().stream().filter(Product::isAvailable).findFirst().orElseThrow();

        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId(customerUser.getId());
        request.setStoreId(store.getId());
        request.setDeliveryArea("Kothrud");
        request.setDeliveryAddress("Flat 102, Green Avenue, Kothrud, Pune 411038");
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        request.setIdempotencyKey(UUID.randomUUID().toString());
        request.setItems(List.of(new OrderItemRequest(product.getId(), 1)));

        Order order = orderService.createOrder(request);
        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        return orderRepository.save(order);
    }

    @Test
    @DisplayName("Delivery Partner Dashboard stats returns valid metrics")
    public void testGetDashboardStats() throws Exception {
        mockMvc.perform(get("/api/v1/delivery/dashboard")
                        .header("Authorization", deliveryPartnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.partnerName").value("Priya Desai"))
                .andExpect(jsonPath("$.data.status").exists());
    }

    @Test
    @DisplayName("Unauthorized roles (Customer, Store Owner) are rejected with 403 Forbidden")
    public void testRoleAuthorizationProtection() throws Exception {
        // Customer cannot access delivery endpoints
        mockMvc.perform(get("/api/v1/delivery/dashboard")
                        .header("Authorization", customerToken))
                .andExpect(status().isForbidden());

        // Store Owner cannot access delivery endpoints
        mockMvc.perform(get("/api/v1/delivery/dashboard")
                        .header("Authorization", storeOwnerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delivery partner can update status between AVAILABLE, OFFLINE, and BUSY")
    public void testStatusUpdate() throws Exception {
        mockMvc.perform(patch("/api/v1/delivery/status?status=OFFLINE")
                        .header("Authorization", deliveryPartnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OFFLINE"));

        mockMvc.perform(patch("/api/v1/delivery/status?status=AVAILABLE")
                        .header("Authorization", deliveryPartnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("Complete delivery lifecycle: Claim -> Pickup -> Out for Delivery -> Delivered")
    public void testFullDeliveryLifecycle() throws Exception {
        Order order = createTestOrderReadyForPickup();
        Long orderId = order.getId();

        // 1. Claim Order
        mockMvc.perform(post("/api/v1/delivery/orders/" + orderId + "/claim")
                        .header("Authorization", deliveryPartnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.data.assignedAt").isNotEmpty());

        // 2. Confirm Pickup
        mockMvc.perform(post("/api/v1/delivery/orders/" + orderId + "/pickup")
                        .header("Authorization", deliveryPartnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PICKED_UP"))
                .andExpect(jsonPath("$.data.pickedUpAt").isNotEmpty());

        // 3. Mark Out for Delivery
        mockMvc.perform(post("/api/v1/delivery/orders/" + orderId + "/out-for-delivery")
                        .header("Authorization", deliveryPartnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OUT_FOR_DELIVERY"));

        // 4. Complete Delivery
        mockMvc.perform(post("/api/v1/delivery/orders/" + orderId + "/complete")
                        .header("Authorization", deliveryPartnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("notes", "Handed over directly to customer"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELIVERED"))
                .andExpect(jsonPath("$.data.deliveredAt").isNotEmpty())
                .andExpect(jsonPath("$.data.deliveryNotes").value("Handed over directly to customer"));

        // 5. Prevent Duplicate Completion
        mockMvc.perform(post("/api/v1/delivery/orders/" + orderId + "/complete")
                        .header("Authorization", deliveryPartnerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already been marked as DELIVERED")));
    }

    @Test
    @DisplayName("Delivery partner reports failed delivery issue")
    public void testDeliveryIssueReporting() throws Exception {
        Order order = createTestOrderReadyForPickup();
        Long orderId = order.getId();

        // Claim
        mockMvc.perform(post("/api/v1/delivery/orders/" + orderId + "/claim")
                        .header("Authorization", deliveryPartnerToken))
                .andExpect(status().isOk());

        // Pickup
        mockMvc.perform(post("/api/v1/delivery/orders/" + orderId + "/pickup")
                        .header("Authorization", deliveryPartnerToken))
                .andExpect(status().isOk());

        // Report Issue
        DeliveryIssueReportRequest request = new DeliveryIssueReportRequest(
                "CUSTOMER_UNREACHABLE",
                "Customer phone was switched off after multiple calling attempts."
        );

        mockMvc.perform(post("/api/v1/delivery/orders/" + orderId + "/report-issue")
                        .header("Authorization", deliveryPartnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELIVERY_FAILED"))
                .andExpect(jsonPath("$.data.deliveryIssueReason").value("CUSTOMER_UNREACHABLE"))
                .andExpect(jsonPath("$.data.deliveryIssueDetails").value("Customer phone was switched off after multiple calling attempts."));
    }
}
