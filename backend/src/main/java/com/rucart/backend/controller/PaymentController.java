package com.rucart.backend.controller;

import com.rucart.backend.dto.ApiResponse;
import com.rucart.backend.dto.PaymentRequest;
import com.rucart.backend.dto.PaymentVerificationRequest;
import com.rucart.backend.model.Payment;
import com.rucart.backend.payment.PaymentOrderResult;
import com.rucart.backend.security.CustomUserDetails;
import com.rucart.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 1. Payment Initiation: Creates payment order/session with gateway (Razorpay).
     */
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentOrderResult>> initiatePayment(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String idempotencyKey = body.get("idempotencyKey") != null ? body.get("idempotencyKey").toString() : null;

        PaymentOrderResult result = paymentService.initiateOnlinePayment(orderId, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.ok("Payment gateway order created", result));
    }

    /**
     * 2. Payment Verification: Validates HMAC SHA256 signature and completes payment.
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Payment>> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        Payment payment = paymentService.verifyAndCompletePayment(request, userId);
        return ResponseEntity.ok(ApiResponse.ok("Payment verified and completed successfully", payment));
    }

    /**
     * 3. Failed/Cancelled Payment Recording.
     */
    @PostMapping("/failed")
    public ResponseEntity<ApiResponse<Payment>> recordFailedPayment(
            @RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String reason = body.get("reason") != null ? body.get("reason").toString() : "Payment rejected or cancelled";

        Payment payment = paymentService.handlePaymentFailure(orderId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Payment failure recorded", payment));
    }

    /**
     * 4. Webhook Handling with signature validation.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        Map<String, Object> result = paymentService.handleWebhook(rawPayload, signature);
        return ResponseEntity.ok(result);
    }

    /**
     * 5. Customer Transaction History.
     */
    @GetMapping("/my-transactions")
    public ResponseEntity<ApiResponse<List<Payment>>> getMyTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Payment> transactions = paymentService.getCustomerTransactions(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(transactions));
    }

    /**
     * 6. Order Payment Details.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByOrderId(@PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.ok(payment));
    }

    /**
     * 7. Receipt Details.
     */
    @GetMapping("/receipt/{receiptNumber}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByReceipt(@PathVariable String receiptNumber) {
        Payment payment = paymentService.getByReceiptNumber(receiptNumber);
        return ResponseEntity.ok(ApiResponse.ok(payment));
    }

    /**
     * 8. Refund Initiation & Processing.
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApiResponse<Payment>> processRefund(
            @PathVariable Long paymentId,
            @RequestBody(required = false) Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BigDecimal refundAmount = null;
        String reason = null;

        if (body != null) {
            if (body.get("amount") != null) {
                refundAmount = new BigDecimal(body.get("amount").toString());
            }
            if (body.get("reason") != null) {
                reason = body.get("reason").toString();
            }
        }

        String phone = userDetails != null ? userDetails.getUsername() : "SYSTEM";
        String role = (userDetails != null && !userDetails.getAuthorities().isEmpty())
                ? userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "") : "ADMIN";

        Payment payment = paymentService.processRefund(paymentId, refundAmount, reason, phone, role);
        return ResponseEntity.ok(ApiResponse.ok("Refund processed successfully", payment));
    }

    /**
     * 9. Downloadable PDF Receipt by Payment ID.
     */
    @GetMapping("/{paymentId}/receipt/pdf")
    public ResponseEntity<byte[]> downloadReceiptPdf(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        boolean isAdmin = userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        byte[] pdfBytes = paymentService.generateReceiptPdf(paymentId, userId, isAdmin);
        Payment payment = paymentService.getPaymentById(paymentId);
        String filename = "RUCART_Receipt_" + payment.getReceiptNumber() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * 10. Downloadable PDF Receipt by Order ID.
     */
    @GetMapping("/order/{orderId}/receipt/pdf")
    public ResponseEntity<byte[]> downloadReceiptPdfByOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        Long userId = userDetails != null ? userDetails.getId() : null;
        boolean isAdmin = userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        byte[] pdfBytes = paymentService.generateReceiptPdf(payment.getId(), userId, isAdmin);
        String filename = "RUCART_Receipt_" + payment.getOrderNumber() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Legacy support for simple COD / manual payment processing.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> processPaymentLegacy(@Valid @RequestBody PaymentRequest request) {
        Payment payment = paymentService.processPayment(request);
        return ResponseEntity.ok(ApiResponse.ok("Payment processed successfully", payment));
    }
}
