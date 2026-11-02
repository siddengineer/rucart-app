package com.rucart.backend.payment;

import com.rucart.backend.model.Order;
import com.rucart.backend.model.Payment;

import java.math.BigDecimal;

public interface PaymentGatewayProvider {

    PaymentOrderResult createPaymentOrder(Order order, BigDecimal amount, String currency, String receiptNumber);

    boolean verifyPaymentSignature(String gatewayOrderId, String gatewayPaymentId, String gatewaySignature);

    boolean verifyWebhookSignature(String rawBody, String signature);

    RefundResult processRefund(Payment payment, BigDecimal refundAmount, String reason);

    String getProviderName();
}
