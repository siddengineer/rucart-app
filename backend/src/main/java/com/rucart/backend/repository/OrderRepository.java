package com.rucart.backend.repository;

import com.rucart.backend.model.Order;
import com.rucart.backend.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, OrderStatus status);
    List<Order> findByStoreIdOrderByCreatedAtDesc(Long storeId);
    List<Order> findByStoreIdAndStatusOrderByCreatedAtDesc(Long storeId, OrderStatus status);
    List<Order> findByDeliveryPartnerIdOrderByCreatedAtDesc(Long deliveryPartnerId);
    List<Order> findByDeliveryPartnerIdAndStatusInOrderByCreatedAtDesc(Long deliveryPartnerId, List<OrderStatus> statuses);
    List<Order> findByDeliveryPartnerIdAndStatusOrderByCreatedAtDesc(Long deliveryPartnerId, OrderStatus status);
    long countByDeliveryPartnerIdAndStatus(Long deliveryPartnerId, OrderStatus status);
    long countByDeliveryPartnerIdAndStatusIn(Long deliveryPartnerId, List<OrderStatus> statuses);
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    List<Order> findByStatusInOrderByCreatedAtDesc(List<OrderStatus> statuses);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);

    long countByStatus(OrderStatus status);
    long countByStoreId(Long storeId);
    long countByStoreIdAndStatus(Long storeId, OrderStatus status);
    long countByCreatedAtAfter(java.time.LocalDateTime start);

    @Query("SELECT COALESCE(SUM(o.grandTotal), 0) FROM Order o WHERE o.status = com.rucart.backend.model.OrderStatus.DELIVERED")
    BigDecimal sumDeliveredOrdersTotal();

    @Query("SELECT COALESCE(SUM(o.grandTotal), 0) FROM Order o WHERE o.store.id = :storeId AND o.status = com.rucart.backend.model.OrderStatus.DELIVERED")
    BigDecimal sumStoreDeliveredOrdersTotal(Long storeId);
}
