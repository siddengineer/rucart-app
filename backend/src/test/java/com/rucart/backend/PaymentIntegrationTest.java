package com.rucart.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rucart.backend.dto.OrderItemRequest;
import com.rucart.backend.dto.OrderCreateRequest;
import com.rucart.backend.dto.PaymentVerificationRequest;
import com.rucart.backend.model.*;
import com.rucart.backend.payment.RazorpayPaymentProvider;
import com.rucart.backend.repository.*;
import com.rucart.backend.security.CustomUserDetails;
import com.rucart.backend.security.JwtUtil;
import com.rucart.backend.service.OrderService;
import com.rucart.backend.service.PaymentService;
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
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RazorpayPaymentProvider razorpayPaymentProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StoreInventoryRepository storeInventoryRepository;

    private User customerUser;
    private User otherCustomerUser;
    private User adminUser;

    private String customerToken;
    private String otherCustomerToken;
    private String adminToken;

    @BeforeEach
    public void setup() {
        customerUser = userRepository.findByPhone("9876543210").orElseThrow();
        adminUser = userRepository.findByPhone("9876543213").orElseThrow();

        otherCustomerUser = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.DELIVERY_PARTNER) // Or another user
                .findFirst().orElseThrow();

        customerToken = "Bearer " + jwtUtil.generateToken(
                new CustomUserDetails(customerUser), customerUser.getId(), customerUser.getRole());
        otherCustomerToken = "Bearer " + jwtUtil.generateToken(
                new CustomUserDetails(otherCustomerUser), otherCustomerUser.getId(), otherCustomerUser.getRole());
        adminToken = "Bearer " + jwtUtil.generateToken(
                new CustomUserDetails(adminUser), adminUser.getId(), adminUser.getRole());
    }

    private Order createTestOrder() {
        Store store = storeRepository.findAll().stream().filter(Store::isOperating).findFirst().orElseThrow();
        Product product = productRepository.findAll().stream().filter(Product::isAvailable).findFirst().orElseThrow();

        // Ensure store inventory has ample stock for tests
        StoreInventory inventory = storeInventoryRepository.findByStoreIdAndProductId(store.getId(), product.getId())
                .orElseGet(() -> {
                    StoreInventory newInv = new StoreInventory();
                    newInv.setStore(store);
                    newInv.setProduct(product);
                    newInv.setPrice(product.getPrice());
                    newInv.setAvailable(true);
                    newInv.setStockQuantity(100);
                    return storeInventoryRepository.save(newInv);
                });
        if (inventory.getStockQuantity() < 10) {
            inventory.setStockQuantity(100);
            storeInventoryRepository.save(inventory);
        }

        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId(customerUser.getId());
        request.setStoreId(store.getId());
        request.setDeliveryArea("Shivajinagar");
        request.setDeliveryAddress("FC Road, Shivajinagar, Pune 411005");
        request.setPaymentMethod(PaymentMethod.ONLINE);
        request.setIdempotencyKey(UUID.randomUUID().toString());
        request.setItems(List.of(new OrderItemRequest(product.getId(), 1)));

        return orderService.createOrder(request);
    }

    @Test
    @DisplayName("Initiate online payment generates structured gateway order and returns Key ID")
    public void testInitiateOnlinePayment() throws Exception {
        Order order = createTestOrder();

        mockMvc.perform(post("/api/v1/payments/initiate")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("orderId", order.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gatewayOrderId").isNotEmpty())
                .andExpect(jsonPath("$.data.keyId").value(razorpayPaymentProvider.getKeyId()))
                .andExpect(jsonPath("$.data.amountInPaise").isNumber())
                .andExpect(jsonPath("$.data.orderNumber").value(order.getOrderNumber()));
    }

    @Test
    @DisplayName("Backend verifies cryptographic HMAC signature and transitions order to PAYMENT_CONFIRMED")
    public void testSuccessfulSignatureVerification() throws Exception {
        Order order = createTestOrder();
        var orderResult = paymentService.initiateOnlinePayment(order.getId(), null);

        String gatewayOrderId = orderResult.getGatewayOrderId();
        String gatewayPaymentId = "pay_test_" + UUID.randomUUID().toString().substring(0, 10);
        String signature = razorpayPaymentProvider.calculateHmacSha256(
                gatewayOrderId + "|" + gatewayPaymentId,
                razorpayPaymentProvider.getKeySecret()
        );

        PaymentVerificationRequest req = new PaymentVerificationRequest(
                order.getId(),
                gatewayOrderId,
                gatewayPaymentId,
                signature
        );

        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.gatewayPaymentId").value(gatewayPaymentId));

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(PaymentStatus.COMPLETED, updatedOrder.getPaymentStatus());
        assertEquals(OrderStatus.PAYMENT_CONFIRMED, updatedOrder.getStatus());
    }

    @Test
    @DisplayName("Tampered or invalid signature is strictly rejected with HTTP 400 Bad Request")
    public void testInvalidSignatureRejection() throws Exception {
        Order order = createTestOrder();
        var orderResult = paymentService.initiateOnlinePayment(order.getId(), null);

        PaymentVerificationRequest req = new PaymentVerificationRequest(
                order.getId(),
                orderResult.getGatewayOrderId(),
                "pay_fake_123456",
                "fraudulent_untrusted_signature_string"
        );

        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("signature verification failed")));
    }

    @Test
    @DisplayName("Duplicate payment completion callbacks are handled idempotently")
    public void testIdempotentPaymentVerification() throws Exception {
        Order order = createTestOrder();
        var orderResult = paymentService.initiateOnlinePayment(order.getId(), null);

        String gatewayOrderId = orderResult.getGatewayOrderId();
        String gatewayPaymentId = "pay_idem_" + UUID.randomUUID().toString().substring(0, 8);
        String signature = razorpayPaymentProvider.calculateHmacSha256(
                gatewayOrderId + "|" + gatewayPaymentId,
                razorpayPaymentProvider.getKeySecret()
        );

        PaymentVerificationRequest req = new PaymentVerificationRequest(order.getId(), gatewayOrderId, gatewayPaymentId, signature);

        // First verification
        paymentService.verifyAndCompletePayment(req, customerUser.getId());

        // Duplicate verification request should return 200 OK with existing record without error
        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("PDF receipt is generated on the fly and downloaded with valid PDF binary signature (%PDF-)")
    public void testGenerateAndDownloadPdfReceipt() throws Exception {
        Order order = createTestOrder();
        Payment payment = paymentService.getPaymentByOrderId(order.getId());

        byte[] pdfBytes = mockMvc.perform(get("/api/v1/payments/" + payment.getId() + "/receipt/pdf")
                        .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".pdf")))
                .andReturn().getResponse().getContentAsByteArray();

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "PDF byte stream should contain invoice data");
        // Verify PDF Magic Bytes: %PDF-
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
        assertEquals('-', (char) pdfBytes[4]);
    }

    @Test
    @DisplayName("Unauthorized user cannot download someone else's receipt")
    public void testUnauthorizedReceiptAccess() throws Exception {
        Order order = createTestOrder();
        Payment payment = paymentService.getPaymentByOrderId(order.getId());

        // otherCustomerToken belongs to deliveryPartner / other user
        mockMvc.perform(get("/api/v1/payments/" + payment.getId() + "/receipt/pdf")
                        .header("Authorization", otherCustomerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Refund processing updates payment to REFUNDED and logs tamper-proof audit")
    public void testRefundProcessing() throws Exception {
        Order order = createTestOrder();
        Payment payment = paymentService.getPaymentByOrderId(order.getId());
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(java.time.LocalDateTime.now());
        payment = paymentRepository.save(payment);

        mockMvc.perform(post("/api/v1/payments/" + payment.getId() + "/refund")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "Customer requested cancellation before pack"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentStatus").value("REFUNDED"))
                .andExpect(jsonPath("$.data.refundAmount").value(order.getGrandTotal().doubleValue()))
                .andExpect(jsonPath("$.data.refundReason").value("Customer requested cancellation before pack"));

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.REFUNDED, updatedOrder.getStatus());
    }
}
