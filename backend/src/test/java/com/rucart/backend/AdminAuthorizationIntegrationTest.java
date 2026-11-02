package com.rucart.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rucart.backend.dto.*;
import com.rucart.backend.model.*;
import com.rucart.backend.repository.*;
import com.rucart.backend.security.CustomUserDetails;
import com.rucart.backend.security.JwtUtil;
import com.rucart.backend.service.AdminService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String customerToken;
    private String storeOwnerToken;
    private String adminToken;

    private User adminUser;
    private User customerUser;
    private User storeOwnerUser;

    @BeforeEach
    public void setup() {
        customerUser = userRepository.findByPhone("9876543210").orElseThrow();
        storeOwnerUser = userRepository.findByPhone("9876543211").orElseThrow();
        adminUser = userRepository.findByPhone("9876543213").orElseThrow();

        customerToken = "Bearer " + jwtUtil.generateToken(new CustomUserDetails(customerUser), customerUser.getId(), customerUser.getRole());
        storeOwnerToken = "Bearer " + jwtUtil.generateToken(new CustomUserDetails(storeOwnerUser), storeOwnerUser.getId(), storeOwnerUser.getRole());
        adminToken = "Bearer " + jwtUtil.generateToken(new CustomUserDetails(adminUser), adminUser.getId(), adminUser.getRole());
    }

    @Test
    @DisplayName("Admin dashboard stats returns accurate live platform counts")
    public void testGetDashboardStats() {
        DashboardStatsDto stats = adminService.getDashboardStats();
        assertNotNull(stats);
        assertTrue(stats.getTotalCustomers() >= 1, "Should count customer");
        assertTrue(stats.getTotalStores() >= 1, "Should count stores");
        assertNotNull(stats.getTotalRevenue(), "Revenue should not be null");
        assertNotNull(stats.getTotalRefunds(), "Refunds should not be null");
    }

    @Test
    @DisplayName("Access denied for unauthenticated requests to admin endpoints")
    public void testUnauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer role is rejected with 403 Forbidden on admin dashboard")
    public void testCustomerCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Store Owner role is rejected with 403 Forbidden on admin endpoints")
    public void testStoreOwnerCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stores")
                        .header("Authorization", storeOwnerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin token can successfully access dashboard and operational reports")
    public void testAdminAccessSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalStores").isNumber());

        mockMvc.perform(get("/api/v1/admin/reports?period=week")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.period").value("week"));
    }

    @Test
    @DisplayName("Admin store creation creates store and generates tamper-proof audit log")
    public void testCreateStoreWithAuditLog() throws Exception {
        long initialLogs = auditLogRepository.count();

        StoreAdminRequest request = new StoreAdminRequest();
        request.setName("Admin Test Branch " + System.currentTimeMillis());
        request.setAddress("MG Road, Camp");
        request.setCity("Pune");
        request.setPhone("9898989898");
        request.setEmail("express@rucart.com");
        request.setServiceAreas("Camp, Pune Station");
        request.setOpeningTime("06:00 AM");
        request.setClosingTime("11:30 PM");
        request.setOwnerId(storeOwnerUser.getId());

        String responseContent = mockMvc.perform(post("/api/v1/admin/stores")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(request.getName()))
                .andReturn().getResponse().getContentAsString();

        // Verify audit log entry was created
        assertTrue(auditLogRepository.count() > initialLogs, "Audit log count must increase");
        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc("CREATE_STORE");
        assertFalse(logs.isEmpty(), "CREATE_STORE audit log must exist");
        assertEquals("STORE", logs.get(0).getTargetType());
        assertEquals("ADMIN", logs.get(0).getActorRole());

        // Clean up created store so other test suites are isolated
        storeRepository.findByNameContainingIgnoreCase("Admin Test Branch").forEach(storeRepository::delete);
    }

    @Test
    @DisplayName("Admin user status update toggles active status and writes audit log")
    public void testToggleUserStatusWithAuditLog() throws Exception {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis() % 10000000);
        User targetUser = new User("Audit User", "91" + uniqueSuffix, "audit" + uniqueSuffix + "@test.com", "pass", Role.CUSTOMER, "Address");
        targetUser = userRepository.save(targetUser);

        UserStatusUpdateRequest updateReq = new UserStatusUpdateRequest(false, "Suspicious activity reported");

        mockMvc.perform(patch("/api/v1/admin/users/" + targetUser.getId() + "/status")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(false));

        User refreshed = userRepository.findById(targetUser.getId()).orElseThrow();
        assertFalse(refreshed.isActive(), "User must be deactivated");

        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc("DEACTIVATE_USER");
        assertFalse(logs.isEmpty(), "DEACTIVATE_USER audit log must be recorded");
        assertEquals(String.valueOf(targetUser.getId()), logs.get(0).getTargetId());
    }

    @Test
    @DisplayName("Admin user endpoint never exposes passwords or password hashes")
    public void testUserPasswordsNotExposedInAdminApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].password").doesNotExist());
    }

    @Test
    @DisplayName("Admin refund process updates payment and order, creating audit log")
    public void testProcessRefundWithAuditLog() throws Exception {
        // Create an order and payment
        Store store = storeRepository.findAll().get(0);
        Order order = new Order();
        order.setOrderNumber("ORD-TEST-" + System.currentTimeMillis());
        order.setCustomer(customerUser);
        order.setCustomerName(customerUser.getName());
        order.setCustomerPhone(customerUser.getPhone());
        order.setStore(store);
        order.setStoreName(store.getName());
        order.setItemTotal(BigDecimal.valueOf(250));
        order.setDeliveryFee(BigDecimal.valueOf(20));
        order.setGrandTotal(BigDecimal.valueOf(270));
        order.setPaymentMethod(PaymentMethod.UPI);
        order.setDeliveryAddress("Pune Test Address");
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order = orderRepository.save(order);

        long uniqueId = System.currentTimeMillis();
        Payment payment = new Payment(order, "REC-TEST-" + uniqueId, "TXN-TEST-" + uniqueId, BigDecimal.valueOf(270), PaymentMethod.UPI, PaymentStatus.COMPLETED);
        payment = paymentRepository.save(payment);

        RefundProcessRequest refundReq = new RefundProcessRequest(BigDecimal.valueOf(270), "Customer requested refund for missing items");

        mockMvc.perform(post("/api/v1/admin/refunds/" + payment.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentStatus").value("REFUNDED"))
                .andExpect(jsonPath("$.data.refundAmount").value(270));

        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(PaymentStatus.REFUNDED, updatedPayment.getPaymentStatus());

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.REFUNDED, updatedOrder.getStatus());

        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc("PROCESS_REFUND");
        assertFalse(logs.isEmpty(), "PROCESS_REFUND audit log must exist");
    }
}
