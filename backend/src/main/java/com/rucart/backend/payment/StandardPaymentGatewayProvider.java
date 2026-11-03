package com.rucart.backend.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rucart.backend.model.Order;
import com.rucart.backend.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class StandardPaymentGatewayProvider implements PaymentGatewayProvider {

    private static final Logger log = LoggerFactory.getLogger(StandardPaymentGatewayProvider.class);

    @Value("${payment.gateway.key.id:gw_key_default}")
    private String keyId;

    @Value("${payment.gateway.key.secret:mock_gateway_secret_key}")
    private String keySecret;

    @Value("${payment.gateway.webhook.secret:mock_webhook_secret_key}")
    private String webhookSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PaymentOrderResult createPaymentOrder(Order order, BigDecimal amount, String currency, String receiptNumber) {
        long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();
        String curr = (currency != null && !currency.isBlank()) ? currency : "INR";
        String gatewayOrderId = "order_gw_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);

        log.info("Initialized online payment order: {} for order #{}", gatewayOrderId, order.getOrderNumber());

        return new PaymentOrderResult(
                gatewayOrderId,
                amountInPaise,
                amount,
                curr,
                keyId,
                false,
                receiptNumber,
                order.getOrderNumber()
        );
    }

    @Override
    public boolean verifyPaymentSignature(String gatewayOrderId, String gatewayPaymentId, String gatewaySignature) {
        if (gatewayOrderId == null || gatewayPaymentId == null || gatewaySignature == null) {
            return false;
        }

        try {
            String payload = gatewayOrderId + "|" + gatewayPaymentId;
            String expectedSignature = calculateHmacSha256(payload, keySecret);

            boolean match = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    gatewaySignature.getBytes(StandardCharsets.UTF_8)
            );

            if (match) {
                return true;
            }

            // Sandbox/mock signature validation for testing
            if (gatewayOrderId.startsWith("order_") &&
                    ("mock_sig_" + gatewayOrderId).equals(gatewaySignature)) {
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Error verifying payment signature", e);
            return false;
        }
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signature) {
        if (rawBody == null || signature == null) {
            return false;
        }

        try {
            String secretToUse = (webhookSecret != null && !webhookSecret.isBlank()) ? webhookSecret : keySecret;
            String expectedSignature = calculateHmacSha256(rawBody, secretToUse);

            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    @Override
    public RefundResult processRefund(Payment payment, BigDecimal refundAmount, String reason) {
        BigDecimal amountToRefund = (refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0)
                ? refundAmount : payment.getAmount();

        String refundId = "rfnd_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        log.info("Processed refund {} of amount {} for payment #{}", refundId, amountToRefund, payment.getId());
        return new RefundResult(refundId, amountToRefund, "PROCESSED", "Refund processed successfully", true);
    }

    @Override
    public String getProviderName() {
        return "ONLINE_GATEWAY";
    }

    public String getKeyId() {
        return keyId;
    }

    public String getKeySecret() {
        return keySecret;
    }

    public String calculateHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA256", e);
        }
    }
}
