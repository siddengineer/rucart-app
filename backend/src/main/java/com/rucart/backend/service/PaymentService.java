package com.rucart.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rucart.backend.dto.PaymentRequest;
import com.rucart.backend.dto.PaymentVerificationRequest;
import com.rucart.backend.model.*;
import com.rucart.backend.payment.PaymentGatewayProvider;
import com.rucart.backend.payment.PaymentOrderResult;
import com.rucart.backend.payment.RefundResult;
import com.rucart.backend.repository.OrderRepository;
import com.rucart.backend.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayProvider paymentGatewayProvider;
    private final ReceiptPdfService receiptPdfService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          PaymentGatewayProvider paymentGatewayProvider,
                          ReceiptPdfService receiptPdfService,
                          AuditLogService auditLogService,
                          NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentGatewayProvider = paymentGatewayProvider;
        this.receiptPdfService = receiptPdfService;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    /**
     * Initializes a standard COD or pending payment for an order.
     */
    @Transactional
    public Payment initializePayment(Order order, PaymentMethod method) {
        Optional<Payment> existing = paymentRepository.findByOrderId(order.getId());
        if (existing.isPresent()) {
            Payment p = existing.get();
            if (method != null) {
                p.setPaymentMethod(method);
            }
            return paymentRepository.save(p);
        }

        String receiptNumber = "REC-" + LocalDateTime.now().getYear() + "-" + (10000 + new Random().nextInt(90000));
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment(
                order,
                receiptNumber,
                transactionId,
                order.getGrandTotal(),
                method != null ? method : PaymentMethod.CASH_ON_DELIVERY,
                PaymentStatus.PENDING
        );
        payment.setOrderNumber(order.getOrderNumber());
        if (order.getCustomer() != null) {
            payment.setCustomer(order.getCustomer());
            payment.setCustomerName(order.getCustomer().getName());
        }

        return paymentRepository.save(payment);
    }

    /**
     * Creates an online payment gateway order/session (e.g. Razorpay order_id)
     * with duplicate payment prevention, backend pricing validation, and idempotency.
     */
    @Transactional
    public PaymentOrderResult initiateOnlinePayment(Long orderId, String idempotencyKey) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot initiate payment for order in terminal status: " + order.getStatus());
        }

        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Order #" + order.getOrderNumber() + " has already been paid.");
        }

        // Idempotency check: if an order session was already generated with this key
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Payment> existingWithKey = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existingWithKey.isPresent() && existingWithKey.get().getGatewayOrderId() != null) {
                Payment p = existingWithKey.get();
                return new PaymentOrderResult(
                        p.getGatewayOrderId(),
                        p.getAmount().multiply(BigDecimal.valueOf(100)).longValue(),
                        p.getAmount(),
                        p.getCurrency(),
                        paymentGatewayProvider instanceof com.rucart.backend.payment.RazorpayPaymentProvider rzp ? rzp.getKeyId() : "test_key",
                        p.isSandbox(),
                        p.getReceiptNumber(),
                        order.getOrderNumber()
                );
            }
        }

        Optional<Payment> existingOpt = paymentRepository.findByOrderId(order.getId());
        Payment payment = existingOpt.orElseGet(() -> initializePayment(order, PaymentMethod.ONLINE));

        // Generate receipt number
        String receiptNumber = payment.getReceiptNumber() != null
                ? payment.getReceiptNumber()
                : "REC-" + LocalDateTime.now().getYear() + "-" + (10000 + new Random().nextInt(90000));

        // Call provider abstraction to create gateway order
        PaymentOrderResult orderResult = paymentGatewayProvider.createPaymentOrder(
                order,
                order.getGrandTotal(),
                "INR",
                receiptNumber
        );

        // Update local transaction metadata
        payment.setGatewayOrderId(orderResult.getGatewayOrderId());
        payment.setPaymentMethod(PaymentMethod.ONLINE);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setSandbox(orderResult.isSandbox());
        payment.setCurrency(orderResult.getCurrency());
        payment.setReceiptNumber(receiptNumber);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            payment.setIdempotencyKey(idempotencyKey);
        }
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return orderResult;
    }

    /**
     * Verifies cryptographic gateway signature and completes payment.
     * Prevents duplicate callbacks and reconciles order/payment status.
     */
    @Transactional
    public Payment verifyAndCompletePayment(PaymentVerificationRequest request, Long authenticatedUserId) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + request.getOrderId()));

        // Security check: verify user ownership
        if (authenticatedUserId != null && order.getCustomer() != null && !order.getCustomer().getId().equals(authenticatedUserId)) {
            throw new SecurityException("Unauthorized: You cannot verify payment for another customer's order");
        }

        Payment payment = paymentRepository.findByGatewayOrderId(request.getGatewayOrderId())
                .orElseGet(() -> paymentRepository.findByOrderId(order.getId())
                        .orElseThrow(() -> new RuntimeException("Payment record not found for order: " + order.getId())));

        // Duplicate prevention / Idempotency check: if already completed, return existing
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            log.info("Payment for order #{} was already marked COMPLETED; returning existing record", order.getOrderNumber());
            return payment;
        }

        // Verify HMAC SHA256 Signature
        boolean signatureValid = paymentGatewayProvider.verifyPaymentSignature(
                request.getGatewayOrderId(),
                request.getGatewayPaymentId(),
                request.getGatewaySignature()
        );

        if (!signatureValid) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("Cryptographic signature verification failed");
            paymentRepository.save(payment);

            auditLogService.logAction(
                    order.getCustomer() != null ? order.getCustomer().getPhone() : "SYSTEM",
                    "CUSTOMER",
                    "PAYMENT_SIGNATURE_FAILED",
                    "PAYMENT",
                    String.valueOf(payment.getId()),
                    "Signature check failed for gatewayOrderId: " + request.getGatewayOrderId(),
                    "FAILED"
            );

            throw new IllegalArgumentException("Payment signature verification failed. Untrusted payment attempt rejected.");
        }

        // Signature Verified -> Transition Payment & Order to COMPLETED
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.ONLINE);
        payment.setGatewayPaymentId(request.getGatewayPaymentId());
        payment.setGatewaySignature(request.getGatewaySignature());
        payment.setTransactionId(request.getGatewayPaymentId());
        payment.setPaidAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        // Update Order Status - Auto-accept order directly upon payment confirmation
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setStatus(OrderStatus.ACCEPTED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Audit & Notify
        auditLogService.logAction(
                order.getCustomer() != null ? order.getCustomer().getPhone() : "SYSTEM",
                "CUSTOMER",
                "PAYMENT_SUCCESS",
                "PAYMENT",
                String.valueOf(savedPayment.getId()),
                "Payment of INR " + savedPayment.getAmount() + " confirmed via " + paymentGatewayProvider.getProviderName() + " (Ref: " + request.getGatewayPaymentId() + ")",
                "SUCCESS"
        );

        if (order.getCustomer() != null) {
            notificationService.sendNotification(
                    order.getCustomer().getId(),
                    "Payment Received!",
                    "Your payment of INR " + savedPayment.getAmount() + " for order #" + order.getOrderNumber() + " has been successfully confirmed.",
                    NotificationType.ORDER_UPDATE,
                    order.getOrderNumber()
            );
        }

        return savedPayment;
    }

    /**
     * Records a payment failure or cancellation.
     */
    @Transactional
    public Payment handlePaymentFailure(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> initializePayment(order, PaymentMethod.ONLINE));

        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            log.warn("Attempted to mark COMPLETED payment as FAILED for order #{}; ignored", order.getOrderNumber());
            return payment;
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setErrorMessage(reason != null ? reason : "Payment cancelled or rejected by user/bank");
        payment.setUpdatedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        order.setPaymentStatus(PaymentStatus.FAILED);
        orderRepository.save(order);

        if (order.getCustomer() != null) {
            notificationService.sendNotification(
                    order.getCustomer().getId(),
                    "Payment Failed",
                    "Payment for order #" + order.getOrderNumber() + " could not be processed: " + payment.getErrorMessage(),
                    NotificationType.ORDER_UPDATE,
                    order.getOrderNumber()
            );
        }

        return saved;
    }

    /**
     * Webhook handling with signature validation and duplicate event protection.
     */
    @Transactional
    public Map<String, Object> handleWebhook(String rawPayload, String signature) {
        if (!paymentGatewayProvider.verifyWebhookSignature(rawPayload, signature)) {
            log.error("Rejected untrusted webhook: Signature verification failed");
            throw new SecurityException("Invalid webhook signature");
        }

        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            String event = root.path("event").asText();
            JsonNode payload = root.path("payload");

            log.info("Processing webhook event: {}", event);

            if ("payment.captured".equals(event) || "order.paid".equals(event)) {
                JsonNode paymentEntity = payload.path("payment").path("entity");
                String rzpOrderId = paymentEntity.path("order_id").asText();
                String rzpPaymentId = paymentEntity.path("id").asText();

                Optional<Payment> paymentOpt = paymentRepository.findByGatewayOrderId(rzpOrderId);
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
                        payment.setPaymentStatus(PaymentStatus.COMPLETED);
                        payment.setGatewayPaymentId(rzpPaymentId);
                        payment.setTransactionId(rzpPaymentId);
                        payment.setPaidAt(LocalDateTime.now());
                        paymentRepository.save(payment);

                        if (payment.getOrder() != null) {
                            Order order = payment.getOrder();
                            order.setPaymentStatus(PaymentStatus.COMPLETED);
                            if (order.getStatus() == OrderStatus.PLACED || order.getStatus() == OrderStatus.PAYMENT_PENDING) {
                                order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
                            }
                            orderRepository.save(order);
                        }
                        log.info("Webhook captured payment for gateway order: {}", rzpOrderId);
                    }
                }
            } else if ("payment.failed".equals(event)) {
                JsonNode paymentEntity = payload.path("payment").path("entity");
                String rzpOrderId = paymentEntity.path("order_id").asText();
                String errorDesc = paymentEntity.path("error_description").asText();

                paymentRepository.findByGatewayOrderId(rzpOrderId).ifPresent(payment -> {
                    if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
                        payment.setPaymentStatus(PaymentStatus.FAILED);
                        payment.setErrorMessage(errorDesc);
                        paymentRepository.save(payment);
                    }
                });
            }

            return Map.of("status", "ok", "event", event);
        } catch (Exception e) {
            log.error("Error processing webhook payload", e);
            throw new RuntimeException("Webhook processing error: " + e.getMessage());
        }
    }

    @Transactional
    public Payment processRefund(Long paymentId, BigDecimal refundAmount, String reason) {
        return processRefund(paymentId, refundAmount, reason, "ADMIN_SYSTEM", "ADMIN");
    }

    /**
     * Controlled refund processing with eligibility validation and audit logging.
     */
    @Transactional
    public Payment processRefund(Long paymentId, BigDecimal refundAmount, String reason, String actorPhone, String actorRole) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Payment #" + payment.getReceiptNumber() + " has already been fully refunded.");
        }

        BigDecimal amount = (refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0)
                ? refundAmount : payment.getAmount();

        if (amount.compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount (" + amount + ") cannot exceed transaction amount (" + payment.getAmount() + ")");
        }

        // Call payment provider to initiate refund
        RefundResult refundResult = paymentGatewayProvider.processRefund(payment, amount, reason);

        payment.setRefundAmount(amount);
        payment.setRefundReason(reason != null ? reason : "Customer or Admin requested refund");
        payment.setRefundedAt(LocalDateTime.now());
        payment.setGatewayRefundId(refundResult.getRefundId());

        if (amount.compareTo(payment.getAmount()) >= 0) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            payment.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
        payment.setUpdatedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        if (payment.getOrder() != null) {
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.REFUNDED);
            order.setPaymentStatus(payment.getPaymentStatus());
            orderRepository.save(order);

            if (order.getCustomer() != null) {
                notificationService.sendNotification(
                        order.getCustomer().getId(),
                        "Refund Processed",
                        "A refund of INR " + amount + " has been initiated for order #" + order.getOrderNumber() + ".",
                        NotificationType.ORDER_UPDATE,
                        order.getOrderNumber()
                );
            }
        }

        auditLogService.logAction(
                actorPhone != null ? actorPhone : "ADMIN_SYSTEM",
                actorRole != null ? actorRole : "ADMIN",
                "REFUND_PROCESSED",
                "PAYMENT",
                String.valueOf(payment.getId()),
                "Refund of INR " + amount + " processed. Ref: " + refundResult.getRefundId() + ". Reason: " + reason,
                "SUCCESS"
        );

        return saved;
    }

    @Transactional
    public Payment processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + request.getOrderId()));

        Optional<Payment> existingOpt = paymentRepository.findByOrderId(order.getId());
        Payment payment = existingOpt.orElseGet(() -> initializePayment(order, request.getPaymentMethod()));

        payment.setPaymentMethod(request.getPaymentMethod());
        if (request.getTransactionId() != null && !request.getTransactionId().isBlank()) {
            payment.setTransactionId(request.getTransactionId());
        }
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());

        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        return paymentRepository.save(payment);
    }

    /**
     * Generates downloadable PDF receipt byte stream with access authorization.
     */
    public byte[] generateReceiptPdf(Long paymentId, Long requestingUserId, boolean isAdmin) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment record not found with id: " + paymentId));

        Order order = payment.getOrder();
        if (order == null) {
            throw new IllegalStateException("Payment has no associated order record");
        }

        // Customer authorization: verify customer ownership
        if (!isAdmin && requestingUserId != null) {
            if (order.getCustomer() != null && !order.getCustomer().getId().equals(requestingUserId)) {
                throw new SecurityException("Unauthorized: You can only download receipts for your own orders");
            }
        }

        return receiptPdfService.generateReceiptPdf(order, payment);
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    public Payment getByReceiptNumber(String receiptNumber) {
        return paymentRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new RuntimeException("Payment receipt not found: " + receiptNumber));
    }

    public List<Payment> getCustomerTransactions(Long customerId) {
        return paymentRepository.findByOrderCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Payment> getAllRefunds() {
        return paymentRepository.findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus.REFUNDED);
    }

    public BigDecimal getTotalConfirmedRevenue() {
        return paymentRepository.sumConfirmedPayments();
    }

    public BigDecimal getTotalRefunds() {
        return paymentRepository.sumRefundedPayments();
    }
}
