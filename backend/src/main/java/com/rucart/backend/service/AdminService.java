package com.rucart.backend.service;

import com.rucart.backend.dto.*;
import com.rucart.backend.model.*;
import com.rucart.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogService auditLogService;
    private final ProductService productService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public AdminService(UserRepository userRepository,
                        StoreRepository storeRepository,
                        OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CategoryRepository categoryRepository,
                        StoreInventoryRepository storeInventoryRepository,
                        PaymentRepository paymentRepository,
                        AuditLogService auditLogService,
                        ProductService productService,
                        OrderService orderService,
                        PaymentService paymentService) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.storeInventoryRepository = storeInventoryRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogService = auditLogService;
        this.productService = productService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    // =========================================================================
    // 1. DASHBOARD & OPERATIONAL REPORTS
    // =========================================================================

    public DashboardStatsDto getDashboardStats() {
        long totalActiveStores = storeRepository.countByActiveTrue();
        long totalStores = storeRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long totalStoreOwners = userRepository.countByRole(Role.STORE_OWNER);
        long totalDeliveryPartners = userRepository.countByRole(Role.DELIVERY_PARTNER);
        long totalProducts = productRepository.count();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long ordersToday = orderRepository.countByCreatedAtAfter(startOfToday);

        long pendingOrders = orderRepository.countByStatus(OrderStatus.PLACED)
                + orderRepository.countByStatus(OrderStatus.PAYMENT_PENDING)
                + orderRepository.countByStatus(OrderStatus.PAYMENT_CONFIRMED)
                + orderRepository.countByStatus(OrderStatus.ACCEPTED)
                + orderRepository.countByStatus(OrderStatus.PREPARING)
                + orderRepository.countByStatus(OrderStatus.PACKED)
                + orderRepository.countByStatus(OrderStatus.READY_FOR_PICKUP)
                + orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY);

        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        BigDecimal totalRevenue = paymentRepository.sumConfirmedPayments();
        if (totalRevenue == null || totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            totalRevenue = orderRepository.sumDeliveredOrdersTotal();
        }
        BigDecimal totalRefunds = paymentRepository.sumRefundedPayments();
        long inventoryAlerts = storeInventoryRepository.findAllLowStockInventories().size();

        return new DashboardStatsDto(
                totalActiveStores,
                totalStores,
                totalCustomers,
                totalStoreOwners,
                totalDeliveryPartners,
                totalProducts,
                ordersToday,
                pendingOrders,
                deliveredOrders,
                cancelledOrders,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                totalRefunds != null ? totalRefunds : BigDecimal.ZERO,
                inventoryAlerts
        );
    }

    public OperationalReportDto getOperationalReports(String period) {
        String filterPeriod = (period != null && !period.isBlank()) ? period.toLowerCase().trim() : "week";
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        switch (filterPeriod) {
            case "today":
                start = LocalDate.now().atStartOfDay();
                break;
            case "month":
                start = LocalDate.now().minusDays(30).atStartOfDay();
                break;
            case "all":
                start = LocalDateTime.of(2020, 1, 1, 0, 0);
                break;
            case "week":
            default:
                start = LocalDate.now().minusDays(7).atStartOfDay();
                filterPeriod = "week";
                break;
        }

        List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);

        long totalOrders = orders.size();
        long completedOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelledOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

        BigDecimal periodRevenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getGrandTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefunds = paymentRepository.sumRefundedPayments();
        long activeStoresCount = storeRepository.countByActiveTrue();
        long totalProductsCount = productRepository.count();
        long lowStockAlertsCount = storeInventoryRepository.findAllLowStockInventories().size();

        // Payment method breakdown
        Map<String, Long> paymentMethodBreakdown = new HashMap<>();
        for (Order order : orders) {
            String method = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "CASH_ON_DELIVERY";
            paymentMethodBreakdown.put(method, paymentMethodBreakdown.getOrDefault(method, 0L) + 1);
        }

        // Store performance calculation
        List<Store> allStores = storeRepository.findAll();
        List<StorePerformanceDto> storePerformances = new ArrayList<>();
        for (Store s : allStores) {
            List<Order> storeOrders = orders.stream()
                    .filter(o -> o.getStore() != null && o.getStore().getId().equals(s.getId()))
                    .collect(Collectors.toList());
            long storeDelivered = storeOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
            BigDecimal storeSales = storeOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                    .map(Order::getGrandTotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            storePerformances.add(new StorePerformanceDto(
                    s.getId(),
                    s.getName(),
                    s.getCity(),
                    s.isActive(),
                    s.isOpen(),
                    storeOrders.size(),
                    storeDelivered,
                    storeSales
            ));
        }

        OperationalReportDto report = new OperationalReportDto();
        report.setPeriod(filterPeriod);
        report.setTotalOrders(totalOrders);
        report.setCompletedOrders(completedOrders);
        report.setCancelledOrders(cancelledOrders);
        report.setTotalRevenue(periodRevenue);
        report.setTotalRefunds(totalRefunds != null ? totalRefunds : BigDecimal.ZERO);
        report.setActiveStoresCount(activeStoresCount);
        report.setTotalProductsCount(totalProductsCount);
        report.setLowStockAlertsCount(lowStockAlertsCount);
        report.setStorePerformances(storePerformances);
        report.setPaymentMethodBreakdown(paymentMethodBreakdown);

        return report;
    }

    // =========================================================================
    // 2. STORE MANAGEMENT
    // =========================================================================

    public List<Store> getAllStores(String search) {
        if (search != null && !search.trim().isEmpty()) {
            return storeRepository.findByNameContainingIgnoreCase(search.trim());
        }
        return storeRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Store createStore(StoreAdminRequest request, String adminActor) {
        Store store = new Store();
        store.setName(request.getName().trim());
        store.setDescription(request.getDescription());
        store.setAddress(request.getAddress().trim());
        store.setCity(request.getCity() != null ? request.getCity().trim() : "Pune");
        store.setPhone(request.getPhone());
        store.setEmail(request.getEmail());
        store.setServiceAreas(request.getServiceAreas() != null ? request.getServiceAreas().trim() : request.getCity());
        store.setContactPerson(request.getContactPerson());
        store.setOpeningTime(request.getOpeningTime() != null ? request.getOpeningTime() : "07:00 AM");
        store.setClosingTime(request.getClosingTime() != null ? request.getClosingTime() : "11:00 PM");
        store.setImageUrl(request.getImageUrl() != null ? request.getImageUrl() : "ic_store");
        store.setActive(request.getActive() != null ? request.getActive() : true);
        store.setOpen(request.getOpen() != null ? request.getOpen() : true);
        store.setApproved(request.getApproved() != null ? request.getApproved() : true);

        if (request.getOwnerId() != null) {
            userRepository.findById(request.getOwnerId()).ifPresent(store::setOwner);
        }
        if (store.getOwner() == null) {
            User defaultOwner = userRepository.findByRole(Role.STORE_OWNER).stream().findFirst()
                    .orElseGet(() -> userRepository.findByPhone(adminActor).orElse(null));
            store.setOwner(defaultOwner);
        }

        Store saved = storeRepository.save(store);
        auditLogService.logAction(adminActor, "ADMIN", "CREATE_STORE", "STORE", String.valueOf(saved.getId()),
                "Created store: " + saved.getName() + " in " + saved.getCity(), "SUCCESS");
        return saved;
    }

    @Transactional
    public Store updateStore(Long id, StoreAdminRequest request, String adminActor) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            store.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            store.setDescription(request.getDescription());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            store.setAddress(request.getAddress().trim());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            store.setCity(request.getCity().trim());
        }
        if (request.getPhone() != null) {
            store.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            store.setEmail(request.getEmail());
        }
        if (request.getServiceAreas() != null) {
            store.setServiceAreas(request.getServiceAreas().trim());
        }
        if (request.getContactPerson() != null) {
            store.setContactPerson(request.getContactPerson());
        }
        if (request.getOpeningTime() != null) {
            store.setOpeningTime(request.getOpeningTime());
        }
        if (request.getClosingTime() != null) {
            store.setClosingTime(request.getClosingTime());
        }
        if (request.getImageUrl() != null) {
            store.setImageUrl(request.getImageUrl());
        }
        if (request.getActive() != null) {
            store.setActive(request.getActive());
        }
        if (request.getOpen() != null) {
            store.setOpen(request.getOpen());
        }
        if (request.getApproved() != null) {
            store.setApproved(request.getApproved());
        }
        if (request.getOwnerId() != null) {
            userRepository.findById(request.getOwnerId()).ifPresent(store::setOwner);
        }
        if (store.getOwner() == null) {
            User defaultOwner = userRepository.findByRole(Role.STORE_OWNER).stream().findFirst()
                    .orElseGet(() -> userRepository.findByPhone(adminActor).orElse(null));
            store.setOwner(defaultOwner);
        }

        Store updated = storeRepository.save(store);
        auditLogService.logAction(adminActor, "ADMIN", "UPDATE_STORE", "STORE", String.valueOf(updated.getId()),
                "Updated store: " + updated.getName(), "SUCCESS");
        return updated;
    }

    @Transactional
    public Store toggleStoreStatus(Long id, Boolean active, Boolean open, String adminActor) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));

        if (active != null) {
            store.setActive(active);
        }
        if (open != null) {
            store.setOpen(open);
        }
        Store updated = storeRepository.save(store);
        auditLogService.logAction(adminActor, "ADMIN", "TOGGLE_STORE_STATUS", "STORE", String.valueOf(updated.getId()),
                "Updated store active=" + updated.isActive() + ", open=" + updated.isOpen(), "SUCCESS");
        return updated;
    }

    // =========================================================================
    // 3. USER MANAGEMENT (CUSTOMER, STORE_OWNER, DELIVERY_PARTNER)
    // =========================================================================

    public List<User> getUsersByRole(Role role, String search) {
        List<User> list = (role != null) ? userRepository.findByRole(role) : userRepository.findAll();
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            list = list.stream()
                    .filter(u -> (u.getName() != null && u.getName().toLowerCase().contains(q))
                            || (u.getPhone() != null && u.getPhone().contains(q))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }
        return list;
    }

    @Transactional
    public User toggleUserStatus(Long userId, UserStatusUpdateRequest request, String adminActor) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setActive(request.isActive());
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        String reason = (request.getReason() != null && !request.getReason().isBlank()) ? request.getReason() : "Admin status update";
        auditLogService.logAction(adminActor, "ADMIN", request.isActive() ? "ACTIVATE_USER" : "DEACTIVATE_USER",
                "USER", String.valueOf(saved.getId()),
                "Changed status of user " + saved.getName() + " (" + saved.getRole() + ") to active=" + saved.isActive() + ". Reason: " + reason,
                "SUCCESS");

        return saved;
    }

    // =========================================================================
    // 4. PRODUCT & CATEGORY MANAGEMENT
    // =========================================================================

    public List<Product> getAllProducts(Long categoryId, String search) {
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc();
        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            products = products.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(q))
                            || (p.getBrand() != null && p.getBrand().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }
        return products;
    }

    @Transactional
    public Product createProduct(ProductCreateRequest request, String adminActor) {
        User adminUser = userRepository.findByPhone(adminActor).orElse(null);
        Product product = productService.createProduct(request, adminUser);
        auditLogService.logAction(adminActor, "ADMIN", "CREATE_PRODUCT", "PRODUCT", String.valueOf(product.getId()),
                "Created product: " + product.getName() + ", Price: ₹" + product.getPrice(), "SUCCESS");
        return product;
    }

    @Transactional
    public Product updateProduct(Long id, ProductUpdateRequest request, String adminActor) {
        Product updated = productService.updateProduct(id, request);
        auditLogService.logAction(adminActor, "ADMIN", "UPDATE_PRODUCT", "PRODUCT", String.valueOf(updated.getId()),
                "Updated product: " + updated.getName(), "SUCCESS");
        return updated;
    }

    @Transactional
    public Product toggleProductAvailability(Long id, boolean available, String adminActor) {
        Product updated = productService.setProductStatus(id, available);
        auditLogService.logAction(adminActor, "ADMIN", "TOGGLE_PRODUCT_AVAILABILITY", "PRODUCT", String.valueOf(updated.getId()),
                "Toggled product available=" + available + " for " + updated.getName(), "SUCCESS");
        return updated;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    public Category createCategory(CategoryRequest request, String adminActor) {
        Category category = new Category();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setIconName(request.getIconName() != null ? request.getIconName().trim() : "ic_category");
        category.setImageUrl(request.getImageUrl());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 1);

        Category saved = categoryRepository.save(category);
        auditLogService.logAction(adminActor, "ADMIN", "CREATE_CATEGORY", "CATEGORY", String.valueOf(saved.getId()),
                "Created category: " + saved.getName(), "SUCCESS");
        return saved;
    }

    @Transactional
    public Category updateCategory(Long id, CategoryRequest request, String adminActor) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            category.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getIconName() != null) {
            category.setIconName(request.getIconName().trim());
        }
        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        Category updated = categoryRepository.save(category);
        auditLogService.logAction(adminActor, "ADMIN", "UPDATE_CATEGORY", "CATEGORY", String.valueOf(updated.getId()),
                "Updated category: " + updated.getName(), "SUCCESS");
        return updated;
    }

    // =========================================================================
    // 5. INVENTORY INSPECTION
    // =========================================================================

    public List<StoreInventory> getStoreInventories(Long storeId, Boolean lowStockOnly) {
        if (storeId != null) {
            if (Boolean.TRUE.equals(lowStockOnly)) {
                return storeInventoryRepository.findLowStockInventories(storeId);
            }
            return storeInventoryRepository.findByStoreId(storeId);
        } else {
            if (Boolean.TRUE.equals(lowStockOnly)) {
                return storeInventoryRepository.findAllLowStockInventories();
            }
            return storeInventoryRepository.findAll();
        }
    }

    // =========================================================================
    // 6. ORDER INSPECTION & STATUS OVERRIDE
    // =========================================================================

    public List<Order> searchOrders(OrderStatus status, Long storeId, String search, String startDate, String endDate) {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();

        if (status != null) {
            orders = orders.stream().filter(o -> o.getStatus() == status).collect(Collectors.toList());
        }
        if (storeId != null) {
            orders = orders.stream().filter(o -> o.getStore() != null && o.getStore().getId().equals(storeId)).collect(Collectors.toList());
        }
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            orders = orders.stream()
                    .filter(o -> (o.getOrderNumber() != null && o.getOrderNumber().toLowerCase().contains(q))
                            || (o.getCustomer() != null && o.getCustomer().getName() != null && o.getCustomer().getName().toLowerCase().contains(q))
                            || (o.getCustomer() != null && o.getCustomer().getPhone() != null && o.getCustomer().getPhone().contains(q)))
                    .collect(Collectors.toList());
        }
        if (startDate != null && !startDate.isBlank()) {
            try {
                LocalDate start = LocalDate.parse(startDate);
                orders = orders.stream()
                        .filter(o -> !o.getCreatedAt().toLocalDate().isBefore(start))
                        .collect(Collectors.toList());
            } catch (Exception ignored) {}
        }
        if (endDate != null && !endDate.isBlank()) {
            try {
                LocalDate end = LocalDate.parse(endDate);
                orders = orders.stream()
                        .filter(o -> !o.getCreatedAt().toLocalDate().isAfter(end))
                        .collect(Collectors.toList());
            } catch (Exception ignored) {}
        }

        return orders;
    }

    public Order getOrderDetails(Long orderId) {
        return orderService.getOrderById(orderId);
    }

    @Transactional
    public Order overrideOrderStatus(Long orderId, OrderStatus newStatus, String reason, String adminActor) {
        Order order = orderService.getOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();

        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            Order cancelled = orderService.cancelOrder(orderId, "Admin cancellation: " + reason, null);
            auditLogService.logAction(adminActor, "ADMIN", "OVERRIDE_ORDER_STATUS", "ORDER",
                    String.valueOf(orderId), "Admin force cancelled order #" + order.getOrderNumber() + ". Reason: " + reason, "SUCCESS");
            return cancelled;
        }

        order.setStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        }
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        auditLogService.logAction(adminActor, "ADMIN", "OVERRIDE_ORDER_STATUS", "ORDER",
                String.valueOf(orderId), "Changed order #" + order.getOrderNumber() + " status from " + oldStatus + " to " + newStatus + ". Reason: " + reason, "SUCCESS");
        return saved;
    }

    // =========================================================================
    // 7. PAYMENTS & REFUNDS
    // =========================================================================

    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    public List<Payment> getAllRefunds() {
        return paymentService.getAllRefunds();
    }

    @Transactional
    public Payment processRefund(Long paymentId, RefundProcessRequest request, String adminActor) {
        Payment refunded = paymentService.processRefund(paymentId, request.getAmount(), request.getReason());
        auditLogService.logAction(adminActor, "ADMIN", "PROCESS_REFUND", "PAYMENT",
                String.valueOf(paymentId), "Processed refund of ₹" + refunded.getRefundAmount() + " for payment #" + paymentId + ". Reason: " + request.getReason(), "SUCCESS");
        return refunded;
    }

    // =========================================================================
    // 8. AUDIT LOGS
    // =========================================================================

    public List<AuditLog> getAuditLogs(String targetType, String action, String actor) {
        if (targetType != null && !targetType.isBlank()) {
            return auditLogService.getAuditLogsByTarget(targetType.trim());
        }
        if (action != null && !action.isBlank()) {
            return auditLogService.getAuditLogsByAction(action.trim());
        }
        if (actor != null && !actor.isBlank()) {
            return auditLogService.getAuditLogsByActor(actor.trim());
        }
        return auditLogService.getAllAuditLogs();
    }
}
