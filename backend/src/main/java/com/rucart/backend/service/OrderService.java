package com.rucart.backend.service;

import com.rucart.backend.dto.OrderCreateRequest;
import com.rucart.backend.dto.OrderItemRequest;
import com.rucart.backend.dto.OrderStatusUpdateRequest;
import com.rucart.backend.dto.StoreAssignmentResult;
import com.rucart.backend.model.*;
import com.rucart.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.ofEntries(
            Map.entry(OrderStatus.CART, Set.of(OrderStatus.PLACED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PLACED, Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.PAYMENT_CONFIRMED, OrderStatus.ACCEPTED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PAYMENT_PENDING, Set.of(OrderStatus.PAYMENT_CONFIRMED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PAYMENT_CONFIRMED, Set.of(OrderStatus.ACCEPTED, OrderStatus.CANCELLED, OrderStatus.REFUND_PENDING)),
            Map.entry(OrderStatus.ACCEPTED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PREPARING, Set.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.PACKED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PACKED, Set.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.ASSIGNED, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.READY_FOR_PICKUP, Set.of(OrderStatus.ASSIGNED, OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.ASSIGNED, Set.of(OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED, OrderStatus.DELIVERY_FAILED)),
            Map.entry(OrderStatus.PICKED_UP, Set.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, OrderStatus.DELIVERY_FAILED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED, OrderStatus.DELIVERY_FAILED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.DELIVERY_FAILED, Set.of(OrderStatus.REFUND_PENDING, OrderStatus.CANCELLED, OrderStatus.ASSIGNED)),
            Map.entry(OrderStatus.CANCELLED, Set.of(OrderStatus.REFUND_PENDING, OrderStatus.REFUNDED)),
            Map.entry(OrderStatus.REFUND_PENDING, Set.of(OrderStatus.REFUNDED)),
            Map.entry(OrderStatus.DELIVERED, Collections.emptySet()),
            Map.entry(OrderStatus.REFUNDED, Collections.emptySet())
    );

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final CustomerAddressRepository addressRepository;
    private final StoreInventoryService inventoryService;
    private final StoreAssignmentService assignmentService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final Msg91Service msg91Service;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        StoreRepository storeRepository,
                        ProductRepository productRepository,
                        CustomerAddressRepository addressRepository,
                        StoreInventoryService inventoryService,
                        StoreAssignmentService assignmentService,
                        PaymentService paymentService,
                        NotificationService notificationService,
                        DeliveryPartnerRepository deliveryPartnerRepository,
                        Msg91Service msg91Service) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.inventoryService = inventoryService;
        this.assignmentService = assignmentService;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.msg91Service = msg91Service;
    }

    /**
     * Create order with atomic inventory reservation, server pricing, idempotency check,
     * snapshots, and pessimistic concurrency protection.
     */
    @Transactional
    public Order createOrder(OrderCreateRequest request) {
        // 1. Idempotency check: prevent duplicate order submissions
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingOrder.isPresent()) {
                log.info("Duplicate order prevented for idempotencyKey: {}", request.getIdempotencyKey());
                return existingOrder.get();
            }
        }

        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + request.getCustomerId()));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order cannot be created with an empty items list");
        }

        // 2. Multi-store assignment and validation
        Store store;
        if (request.getStoreId() != null) {
            store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Store not found with id: " + request.getStoreId()));
            if (!store.isOperating()) {
                throw new IllegalStateException("Store '" + store.getName() + "' is currently closed or inactive and cannot accept orders.");
            }
        } else {
            // Automatic store assignment based on deliveryArea and inventory fulfillment
            StoreAssignmentResult assignResult = assignmentService.assignStoreForOrder(request.getDeliveryArea(), request.getItems());
            if (!assignResult.isCanFulfill() || assignResult.getAssignedStore() == null) {
                throw new IllegalStateException(assignResult.getMessage());
            }
            store = assignResult.getAssignedStore();
        }

        // 3. Generate unique order number like #RUC12345
        String orderNumber = "RUC" + (10000 + new Random().nextInt(90000));
        while (orderRepository.findByOrderNumber(orderNumber).isPresent()) {
            orderNumber = "RUC" + (10000 + new Random().nextInt(90000));
        }

        // 4. Generate 4 digit delivery OTP
        String otp = String.format("%04d", new Random().nextInt(10000));

        Order order = new Order();
        order.setCustomer(customer);
        order.setCustomerName(customer.getName());
        order.setCustomerPhone(customer.getPhone());
        order.setStore(store);
        order.setStoreName(store.getName());
        order.setStatus(OrderStatus.ACCEPTED);
        PaymentMethod selectedMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH_ON_DELIVERY;
        order.setPaymentMethod(selectedMethod);
        if (selectedMethod == PaymentMethod.UPI || selectedMethod == PaymentMethod.ONLINE) {
            order.setPaymentStatus(PaymentStatus.PENDING_VERIFICATION);
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);
        }
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setOrderNumber(orderNumber);
        order.setDeliveryOtp(otp);
        order.setIdempotencyKey(request.getIdempotencyKey());

        // Resolve delivery address snapshot
        if (request.getCustomerAddressId() != null) {
            CustomerAddress address = addressRepository.findById(request.getCustomerAddressId()).orElse(null);
            if (address != null) {
                order.setCustomerAddress(address);
                order.setDeliveryAddress(address.getFormattedAddress());
            }
        }
        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().isBlank()) {
            order.setDeliveryAddress(request.getDeliveryAddress() != null && !request.getDeliveryAddress().isBlank()
                    ? request.getDeliveryAddress()
                    : (customer.getAddress() != null ? customer.getAddress() : "Shivajinagar, Pune"));
        }

        BigDecimal itemTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 5. Reserve stock for each item using pessimistic write lock and compute prices exclusively on server
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemReq.getProductId()));

            // Deduct & reserve stock with pessimistic write lock
            StoreInventory reservedInv = inventoryService.reserveStockWithLock(
                    store.getId(),
                    product.getId(),
                    itemReq.getQuantity(),
                    orderNumber,
                    customer
            );

            // Server-calculated store-specific price
            BigDecimal unitPrice = reservedInv.getPrice() != null ? reservedInv.getPrice() : product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            itemTotal = itemTotal.add(lineTotal);

            // OrderItem snapshot with product details
            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    product.getName(),
                    product.getUnit(),
                    itemReq.getQuantity(),
                    unitPrice,
                    lineTotal
            );
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setItemTotal(itemTotal);
        order.setDeliveryFee(new BigDecimal("20.00"));
        order.setDiscount(BigDecimal.ZERO);
        order.setGrandTotal(itemTotal.add(order.getDeliveryFee()));

        Order savedOrder = orderRepository.save(order);

        // Initialize payment and receipt
        paymentService.initializePayment(savedOrder, order.getPaymentMethod());

        // Send notifications
        notificationService.sendNotification(
                customer.getId(),
                "Order Placed!",
                "Your order #" + savedOrder.getOrderNumber() + " has been placed with " + store.getName(),
                NotificationType.ORDER_UPDATE,
                savedOrder.getOrderNumber()
        );

        if (store.getOwner() != null) {
            notificationService.sendNotification(
                store.getOwner().getId(),
                "New Order Alert!",
                "Incoming order #" + savedOrder.getOrderNumber() + " received. Total: ₹" + savedOrder.getGrandTotal(),
                NotificationType.STORE_ALERT,
                savedOrder.getOrderNumber()
            );
        }

        return savedOrder;
    }

    /**
     * Cancel order and release reserved stock back into inventory.
     */
    @Transactional
    public Order cancelOrder(Long orderId, String reason, Long cancelledByUserId) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order #" + order.getOrderNumber() + " is already cancelled");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivered orders cannot be cancelled");
        }
        if (order.getStatus() == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Refunded orders cannot be cancelled");
        }

        User user = null;
        if (cancelledByUserId != null) {
            user = userRepository.findById(cancelledByUserId).orElse(null);
        }

        // Release stock for each item in the order
        for (OrderItem item : order.getItems()) {
            inventoryService.releaseStockWithLock(
                    order.getStore().getId(),
                    item.getProduct().getId(),
                    item.getQuantity(),
                    order.getOrderNumber(),
                    user,
                    reason
            );
        }

        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            order.setStatus(OrderStatus.REFUND_PENDING);
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            order.setPaymentStatus(PaymentStatus.FAILED);
        }
        order.setCancellationReason(reason != null ? reason : "Order cancelled");
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);

        // Notify customer
        notificationService.sendNotification(
                order.getCustomer().getId(),
                "Order Cancelled",
                "Your order #" + order.getOrderNumber() + " has been cancelled. Any reserved items have been restored.",
                NotificationType.ORDER_UPDATE,
                order.getOrderNumber()
        );

        return updated;
    }

    /**
     * Validates if a transition from currentStatus to newStatus is permissible.
     */
    public boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == null || newStatus == null) return false;
        if (currentStatus == newStatus) return true;
        Set<OrderStatus> allowed = VALID_TRANSITIONS.get(currentStatus);
        return allowed != null && allowed.contains(newStatus);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = getOrderById(orderId);

        if (request.getStatus() != null) {
            OrderStatus newStatus = request.getStatus();
            if (!isValidStatusTransition(order.getStatus(), newStatus)) {
                throw new IllegalArgumentException(
                        "Invalid order status transition from " + order.getStatus() + " to " + newStatus
                );
            }

            if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
                return cancelOrder(orderId, "Status updated to CANCELLED", null);
            }

            order.setStatus(newStatus);
            if (newStatus == OrderStatus.DELIVERED) {
                order.setPaymentStatus(PaymentStatus.COMPLETED);
            }
        }

        if (request.getDeliveryPartnerId() != null) {
            User partner = userRepository.findById(request.getDeliveryPartnerId())
                    .orElseThrow(() -> new RuntimeException("Delivery partner not found: " + request.getDeliveryPartnerId()));
            order.setDeliveryPartner(partner);
        }

        order.setUpdatedAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);

        // Notify customer on status progression
        notificationService.sendNotification(
                order.getCustomer().getId(),
                "Order Update: " + order.getStatus().name(),
                "Your order #" + order.getOrderNumber() + " is now " + order.getStatus().name().replace('_', ' '),
                NotificationType.ORDER_UPDATE,
                order.getOrderNumber()
        );

        return updated;
    }

    @Transactional
    public Order acceptOrder(Long orderId, Long storeOwnerUserId) {
        Order order = getOrderById(orderId);
        verifyStoreOwnerAccess(order, storeOwnerUserId);

        if (order.getStatus() == OrderStatus.ACCEPTED) {
            return order;
        }
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.PAYMENT_CONFIRMED) {
            throw new IllegalStateException("Cannot accept order in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.ACCEPTED);
        order.setUpdatedAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);

        notificationService.sendNotification(
                order.getCustomer().getId(),
                "Order Accepted!",
                "Your order #" + order.getOrderNumber() + " has been accepted by " + order.getStore().getName(),
                NotificationType.ORDER_UPDATE,
                order.getOrderNumber()
        );
        return updated;
    }

    @Transactional
    public Order rejectOrder(Long orderId, String reason, Long storeOwnerUserId) {
        Order order = getOrderById(orderId);
        verifyStoreOwnerAccess(order, storeOwnerUserId);

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reject order in status: " + order.getStatus());
        }

        String rejectReason = "Rejected by store: " + (reason != null ? reason : "Store busy or out of stock");
        return cancelOrder(orderId, rejectReason, storeOwnerUserId);
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Order> getCustomerOrdersFiltered(Long customerId, OrderStatus status) {
        if (status == null) {
            return getOrdersByCustomer(customerId);
        }
        return orderRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status);
    }

    public List<Order> getOrdersByStore(Long storeId) {
        return orderRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
    }

    public List<Order> getStoreOrdersFiltered(Long storeId, OrderStatus status) {
        if (status == null) {
            return getOrdersByStore(storeId);
        }
        return orderRepository.findByStoreIdAndStatusOrderByCreatedAtDesc(storeId, status);
    }

    public List<Order> getOrdersByDeliveryPartner(Long deliveryPartnerId) {
        return orderRepository.findByDeliveryPartnerIdOrderByCreatedAtDesc(deliveryPartnerId);
    }

    public List<Order> getAvailableOrdersForDelivery() {
        return orderRepository.findByStatusInOrderByCreatedAtDesc(
                List.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.PACKED, OrderStatus.PREPARING)
        );
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public void verifyCustomerOwnership(Order order, Long customerId) {
        if (customerId != null && !order.getCustomer().getId().equals(customerId)) {
            throw new SecurityException("Unauthorized: This order belongs to another customer");
        }
    }

    public void verifyStoreOwnerAccess(Order order, Long storeOwnerUserId) {
        if (storeOwnerUserId != null && order.getStore().getOwner() != null) {
            if (!order.getStore().getOwner().getId().equals(storeOwnerUserId)) {
                throw new SecurityException("Unauthorized: You are not authorized to manage orders for this store");
            }
        }
    }

    public void verifyDeliveryPartnerAccess(Order order, Long partnerUserId) {
        if (partnerUserId != null) {
            if (order.getDeliveryPartner() == null || !order.getDeliveryPartner().getId().equals(partnerUserId)) {
                throw new SecurityException("Unauthorized: You are not the assigned delivery partner for order #" + order.getOrderNumber());
            }
        }
    }

    @Transactional
    public Order assignDeliveryPartner(Long orderId, Long partnerUserId) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot claim order in terminal status: " + order.getStatus());
        }

        if (order.getDeliveryPartner() != null && !order.getDeliveryPartner().getId().equals(partnerUserId)) {
            throw new IllegalStateException("Order #" + order.getOrderNumber() + " has already been claimed by another delivery partner.");
        }

        User partnerUser = userRepository.findById(partnerUserId)
                .orElseThrow(() -> new RuntimeException("Delivery partner user not found with id: " + partnerUserId));

        order.setDeliveryPartner(partnerUser);
        order.setStatus(OrderStatus.ASSIGNED);
        order.setAssignedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);

        deliveryPartnerRepository.findByUserId(partnerUserId).ifPresent(dp -> {
            dp.setStatus(DeliveryPartnerStatus.BUSY);
            deliveryPartnerRepository.save(dp);
        });

        notificationService.sendNotification(
                order.getCustomer().getId(),
                "Delivery Partner Assigned",
                "Delivery partner " + partnerUser.getName() + " has accepted your order #" + order.getOrderNumber() + ".",
                NotificationType.ORDER_UPDATE,
                order.getOrderNumber()
        );

        return updated;
    }

    @Transactional
    public Order confirmPickup(Long orderId, Long partnerUserId) {
        Order order = getOrderById(orderId);
        verifyDeliveryPartnerAccess(order, partnerUserId);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order #" + order.getOrderNumber() + " has already been delivered.");
        }
        if (order.getStatus() != OrderStatus.ASSIGNED && order.getStatus() != OrderStatus.READY_FOR_PICKUP && order.getStatus() != OrderStatus.PACKED) {
            throw new IllegalStateException("Cannot confirm pickup from current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PICKED_UP);
        order.setPickedUpAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);

        notificationService.sendNotification(
                order.getCustomer().getId(),
                "Order Picked Up!",
                "Your order #" + order.getOrderNumber() + " has been picked up from " + order.getStore().getName() + ".",
                NotificationType.ORDER_UPDATE,
                order.getOrderNumber()
        );

        return updated;
    }

    @Transactional
    public Order startDelivery(Long orderId, Long partnerUserId) {
        Order order = getOrderById(orderId);
        verifyDeliveryPartnerAccess(order, partnerUserId);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order #" + order.getOrderNumber() + " has already been delivered.");
        }
        if (order.getStatus() != OrderStatus.PICKED_UP && order.getStatus() != OrderStatus.ASSIGNED) {
            throw new IllegalStateException("Order must be picked up before setting to out for delivery. Current: " + order.getStatus());
        }

        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setUpdatedAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);

        notificationService.sendNotification(
                order.getCustomer().getId(),
                "Out for Delivery",
                "Your order #" + order.getOrderNumber() + " is out for delivery with our delivery partner.",
                NotificationType.ORDER_UPDATE,
                order.getOrderNumber()
        );

        return updated;
    }

    @Transactional
    public Order completeDelivery(Long orderId, Long partnerUserId, String notes) {
        return completeDelivery(orderId, partnerUserId, null, notes);
    }

    @Transactional
    public Order completeDelivery(Long orderId, Long partnerUserId, String otp, String notes) {
        Order order = getOrderById(orderId);
        verifyDeliveryPartnerAccess(order, partnerUserId);

        // Prevent duplicate completion
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order #" + order.getOrderNumber() + " has already been marked as DELIVERED.");
        }
        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY && order.getStatus() != OrderStatus.PICKED_UP) {
            throw new IllegalStateException("Cannot complete delivery from status: " + order.getStatus());
        }

        // Verify customer verification OTP if set
        if (otp != null && !otp.isBlank()) {
            if (order.getDeliveryOtp() != null && !order.getDeliveryOtp().trim().equalsIgnoreCase(otp.trim())) {
                throw new IllegalArgumentException("Invalid delivery verification code. Please ask the customer for the correct 4-digit OTP.");
            }
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            order.setDeliveryNotes(notes.trim());
        }
        if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY && order.getPaymentStatus() == PaymentStatus.PENDING) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        }
        order.setUpdatedAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);

        deliveryPartnerRepository.findByUserId(partnerUserId).ifPresent(dp -> {
            dp.setTotalDeliveries((dp.getTotalDeliveries() != null ? dp.getTotalDeliveries() : 0) + 1);
            BigDecimal earnings = order.getDeliveryFee() != null ? order.getDeliveryFee() : new BigDecimal("35.00");
            dp.setEarningsTotal((dp.getEarningsTotal() != null ? dp.getEarningsTotal() : BigDecimal.ZERO).add(earnings));

            long remainingActive = orderRepository.countByDeliveryPartnerIdAndStatusIn(
                    partnerUserId,
                    List.of(OrderStatus.ASSIGNED, OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY)
            );
            if (remainingActive == 0) {
                dp.setStatus(DeliveryPartnerStatus.AVAILABLE);
            }
            deliveryPartnerRepository.save(dp);
        });

        notificationService.sendNotification(
                order.getCustomer().getId(),
                "Order Delivered!",
                "Your order #" + order.getOrderNumber() + " has been successfully delivered. Enjoy your groceries!",
                NotificationType.ORDER_UPDATE,
                order.getOrderNumber()
        );

        return updated;
    }

    /**
     * Generates a fresh 4-digit verification OTP and sends it to the customer via MSG91 SMS & notification.
     */
    @Transactional
    public Order generateAndSendDeliveryOtp(Long orderId, Long partnerUserId) {
        Order order = getOrderById(orderId);
        verifyDeliveryPartnerAccess(order, partnerUserId);

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot generate delivery OTP for order in status: " + order.getStatus());
        }

        String otp = String.format("%04d", new Random().nextInt(10000));
        order.setDeliveryOtp(otp);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        String customerPhone = order.getCustomerPhone();
        if (customerPhone != null && !customerPhone.isBlank()) {
            msg91Service.sendOtpSms(customerPhone, otp, "Order #" + order.getOrderNumber() + " Delivery Verification");
        }

        if (order.getCustomer() != null) {
            notificationService.sendNotification(
                    order.getCustomer().getId(),
                    "Delivery Verification Code",
                    "Your delivery OTP for Order #" + order.getOrderNumber() + " is " + otp + ". Share this code with the delivery partner upon arrival.",
                    NotificationType.ORDER_UPDATE,
                    order.getOrderNumber()
            );
        }

        log.info("Delivery partner #{} generated OTP for Order #{}", partnerUserId, order.getOrderNumber());
        return saved;
    }

    @Transactional
    public Order reportDeliveryIssue(Long orderId, Long partnerUserId, String issueType, String details) {
        Order order = getOrderById(orderId);
        verifyDeliveryPartnerAccess(order, partnerUserId);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot report delivery issue on an already delivered order.");
        }

        order.setStatus(OrderStatus.DELIVERY_FAILED);
        order.setDeliveryIssueReason(issueType != null && !issueType.isBlank() ? issueType : "DELIVERY_ATTEMPT_FAILED");
        order.setDeliveryIssueDetails(details);
        order.setDeliveryIssueAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);

        deliveryPartnerRepository.findByUserId(partnerUserId).ifPresent(dp -> {
            long remainingActive = orderRepository.countByDeliveryPartnerIdAndStatusIn(
                    partnerUserId,
                    List.of(OrderStatus.ASSIGNED, OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY)
            );
            if (remainingActive == 0) {
                dp.setStatus(DeliveryPartnerStatus.AVAILABLE);
            }
            deliveryPartnerRepository.save(dp);
        });

        notificationService.sendNotification(
                order.getCustomer().getId(),
                "Delivery Issue Reported",
                "Delivery attempt for order #" + order.getOrderNumber() + " could not be completed: " + (details != null ? details : issueType),
                NotificationType.ORDER_UPDATE,
                order.getOrderNumber()
        );

        if (order.getStore().getOwner() != null) {
            notificationService.sendNotification(
                    order.getStore().getOwner().getId(),
                    "Delivery Issue on Order #" + order.getOrderNumber(),
                    "Partner reported issue: " + (details != null ? details : issueType),
                    NotificationType.ORDER_UPDATE,
                    order.getOrderNumber()
            );
        }

        return updated;
    }

    public List<Order> getActiveDeliveriesForPartner(Long partnerUserId) {
        return orderRepository.findByDeliveryPartnerIdAndStatusInOrderByCreatedAtDesc(
                partnerUserId,
                List.of(OrderStatus.ASSIGNED, OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY)
        );
    }

    public List<Order> getDeliveryHistoryForPartner(Long partnerUserId, String period) {
        List<Order> allHistory = orderRepository.findByDeliveryPartnerIdAndStatusInOrderByCreatedAtDesc(
                partnerUserId,
                List.of(OrderStatus.DELIVERED, OrderStatus.DELIVERY_FAILED)
        );

        if ("today".equalsIgnoreCase(period)) {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            return allHistory.stream()
                    .filter(o -> (o.getDeliveredAt() != null && o.getDeliveredAt().isAfter(startOfDay))
                            || (o.getDeliveryIssueAt() != null && o.getDeliveryIssueAt().isAfter(startOfDay))
                            || (o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfDay)))
                    .toList();
        } else if ("week".equalsIgnoreCase(period)) {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            return allHistory.stream()
                    .filter(o -> (o.getDeliveredAt() != null && o.getDeliveredAt().isAfter(sevenDaysAgo))
                            || (o.getDeliveryIssueAt() != null && o.getDeliveryIssueAt().isAfter(sevenDaysAgo))
                            || (o.getCreatedAt() != null && o.getCreatedAt().isAfter(sevenDaysAgo)))
                    .toList();
        }

        return allHistory;
    }
}
