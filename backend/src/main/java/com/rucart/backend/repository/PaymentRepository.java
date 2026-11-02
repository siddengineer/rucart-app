package com.rucart.backend.repository;

import com.rucart.backend.model.Payment;
import com.rucart.backend.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByReceiptNumber(String receiptNumber);
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    List<Payment> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Payment> findByOrderCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Payment> findAllByOrderByCreatedAtDesc();
    List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus status);
    long countByPaymentStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = com.rucart.backend.model.PaymentStatus.COMPLETED")
    BigDecimal sumConfirmedPayments();

    @Query("SELECT COALESCE(SUM(p.refundAmount), 0) FROM Payment p WHERE p.paymentStatus = com.rucart.backend.model.PaymentStatus.REFUNDED")
    BigDecimal sumRefundedPayments();
}
