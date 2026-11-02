package com.rucart.backend;

import com.rucart.backend.dto.*;
import com.rucart.backend.model.*;
import com.rucart.backend.repository.*;
import com.rucart.backend.service.OrderService;
import com.rucart.backend.service.StoreAssignmentService;
import com.rucart.backend.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MultiStoreAndOrderWorkflowIntegrationTest {

    @Autowired
    private StoreAssignmentService storeAssignmentService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreInventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testCustomer;
    private User testStoreOwner;
    private Store storeShivajinagar;
    private Store storeKothrud;
    private Product testProduct;

    @BeforeEach
    public void setup() {
        testCustomer = userRepository.findByPhone("9876543210").orElse(null);
        if (testCustomer == null) {
            testCustomer = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.CUSTOMER)
                    .findFirst()
                    .orElseGet(() -> userRepository.save(new User("Test Customer", "9876543210", "cust@test.com", "pass", Role.CUSTOMER, "Shivajinagar, Pune")));
        }

        testStoreOwner = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STORE_OWNER)
                .findFirst()
                .orElseGet(() -> userRepository.save(new User("Test Owner", "9876543211", "owner@test.com", "pass", Role.STORE_OWNER, "Shivajinagar, Pune")));

        List<Store> stores = storeRepository.findAll();
        for (Store s : stores) {
            if (storeShivajinagar == null && (s.getName().contains("Express") || s.getAddress().contains("Shivajinagar"))) {
                storeShivajinagar = s;
                storeShivajinagar.setOpen(true);
                storeShivajinagar.setActive(true);
                storeShivajinagar.setServiceAreas("Shivajinagar, FC Road, Model Colony, Deccan");
                storeRepository.save(storeShivajinagar);
            } else if (storeKothrud == null && (s.getName().contains("Kothrud") || s.getAddress().contains("Kothrud"))) {
                storeKothrud = s;
                storeKothrud.setOpen(true);
                storeKothrud.setActive(true);
                storeKothrud.setServiceAreas("Kothrud, Paud Road, Karve Nagar, Warje");
                storeRepository.save(storeKothrud);
            }
        }

        List<Product> products = productRepository.findAll();
        if (!products.isEmpty()) {
            testProduct = products.get(0);
        }
    }

    @org.junit.jupiter.api.AfterEach
    public void cleanup() {
        for (Store s : storeRepository.findAll()) {
            s.setOpen(true);
            s.setActive(true);
            storeRepository.save(s);
        }
    }

    @Test
    @DisplayName("Test 1: Store Assignment selects suitable store based on customer delivery area")
    public void testStoreAssignmentByDeliveryArea() {
        assertNotNull(storeShivajinagar);
        assertNotNull(storeKothrud);
        assertNotNull(testProduct);

        // Ensure both stores have stock
        inventoryRepository.findByStoreIdAndProductId(storeShivajinagar.getId(), testProduct.getId())
                .ifPresent(inv -> { inv.setStockQuantity(50); inv.setAvailable(true); inventoryRepository.save(inv); });
        inventoryRepository.findByStoreIdAndProductId(storeKothrud.getId(), testProduct.getId())
                .ifPresent(inv -> { inv.setStockQuantity(50); inv.setAvailable(true); inventoryRepository.save(inv); });

        List<OrderItemRequest> items = List.of(new OrderItemRequest(testProduct.getId(), 2));

        // 1. Customer in Kothrud
        StoreAssignmentResult kothrudResult = storeAssignmentService.assignStoreForOrder("Kothrud, Pune", items);
        assertTrue(kothrudResult.isCanFulfill());
        assertNotNull(kothrudResult.getAssignedStore());
        assertEquals(storeKothrud.getId(), kothrudResult.getAssignedStore().getId(),
                "Order in Kothrud should be assigned to Fresh Basket Kothrud");

        // 2. Customer in FC Road / Shivajinagar
        StoreAssignmentResult shivajinagarResult = storeAssignmentService.assignStoreForOrder("FC Road, Shivajinagar", items);
        assertTrue(shivajinagarResult.isCanFulfill());
        assertNotNull(shivajinagarResult.getAssignedStore());
        assertEquals(storeShivajinagar.getId(), shivajinagarResult.getAssignedStore().getId(),
                "Order in FC Road should be assigned to RUCART Express Store");
    }

    @Test
    @DisplayName("Test 2: Prevention of assigning order to closed or inactive store")
    public void testPreventionOfOrderToClosedOrInactiveStore() {
        assertNotNull(storeShivajinagar);

        // Temporarily close store
        storeShivajinagar.setOpen(false);
        storeRepository.save(storeShivajinagar);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(testCustomer.getId());
        req.setStoreId(storeShivajinagar.getId());
        req.setDeliveryAddress("Shivajinagar, Pune");
        req.setItems(List.of(new OrderItemRequest(testProduct.getId(), 1)));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            orderService.createOrder(req);
        });

        assertTrue(exception.getMessage().contains("closed or inactive"),
                "Exception message should mention store is closed or inactive: " + exception.getMessage());

        // Restore store to open
        storeShivajinagar.setOpen(true);
        storeRepository.save(storeShivajinagar);
    }

    @Test
    @DisplayName("Test 3: Server computes prices and creates immutable snapshots")
    public void testServerPriceComputationAndSnapshots() {
        assertNotNull(storeShivajinagar);
        assertNotNull(testProduct);

        // Ensure stock is available
        StoreInventory inv = inventoryRepository.findByStoreIdAndProductId(storeShivajinagar.getId(), testProduct.getId())
                .orElseGet(() -> inventoryRepository.save(new StoreInventory(storeShivajinagar, testProduct, new BigDecimal("35.00"), 50, 5)));
        inv.setStockQuantity(50);
        inv.setPrice(new BigDecimal("45.00"));
        inventoryRepository.save(inv);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(testCustomer.getId());
        req.setStoreId(storeShivajinagar.getId());
        req.setDeliveryAddress("Lane 4, FC Road, Pune");
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        req.setItems(List.of(new OrderItemRequest(testProduct.getId(), 3)));

        Order order = orderService.createOrder(req);

        assertNotNull(order.getId());
        assertNotNull(order.getOrderNumber());
        assertEquals(OrderStatus.PLACED, order.getStatus());

        // Verify Snapshots
        assertEquals(testCustomer.getName(), order.getCustomerName());
        assertEquals(storeShivajinagar.getName(), order.getStoreName());
        assertEquals(1, order.getItems().size());

        OrderItem itemSnapshot = order.getItems().get(0);
        assertEquals(testProduct.getName(), itemSnapshot.getProductName());
        assertEquals(3, itemSnapshot.getQuantity());
        assertEquals(new BigDecimal("45.00"), itemSnapshot.getUnitPrice());
        assertEquals(new BigDecimal("135.00"), itemSnapshot.getTotalPrice());

        // Grand total = 135 + 20 delivery fee = 155
        assertEquals(new BigDecimal("135.00"), order.getItemTotal());
        assertEquals(new BigDecimal("155.00"), order.getGrandTotal());
    }

    @Test
    @DisplayName("Test 4: State machine rejects invalid order status transitions")
    public void testStateMachineRejectionOfInvalidTransitions() {
        assertNotNull(storeShivajinagar);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(testCustomer.getId());
        req.setStoreId(storeShivajinagar.getId());
        req.setDeliveryAddress("Shivajinagar, Pune");
        req.setItems(List.of(new OrderItemRequest(testProduct.getId(), 1)));

        Order order = orderService.createOrder(req);
        assertEquals(OrderStatus.PLACED, order.getStatus());

        // 1. Advance validly: PLACED -> ACCEPTED -> PREPARING -> READY_FOR_PICKUP -> OUT_FOR_DELIVERY -> DELIVERED
        OrderStatusUpdateRequest step1 = new OrderStatusUpdateRequest();
        step1.setStatus(OrderStatus.ACCEPTED);
        order = orderService.updateOrderStatus(order.getId(), step1);
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());

        OrderStatusUpdateRequest step2 = new OrderStatusUpdateRequest();
        step2.setStatus(OrderStatus.PREPARING);
        order = orderService.updateOrderStatus(order.getId(), step2);
        assertEquals(OrderStatus.PREPARING, order.getStatus());

        OrderStatusUpdateRequest step3 = new OrderStatusUpdateRequest();
        step3.setStatus(OrderStatus.READY_FOR_PICKUP);
        order = orderService.updateOrderStatus(order.getId(), step3);
        assertEquals(OrderStatus.READY_FOR_PICKUP, order.getStatus());

        OrderStatusUpdateRequest step4 = new OrderStatusUpdateRequest();
        step4.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        order = orderService.updateOrderStatus(order.getId(), step4);
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());

        OrderStatusUpdateRequest step5 = new OrderStatusUpdateRequest();
        step5.setStatus(OrderStatus.DELIVERED);
        order = orderService.updateOrderStatus(order.getId(), step5);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());

        // 2. DELIVERED order cannot be cancelled!
        OrderStatusUpdateRequest invalidCancel = new OrderStatusUpdateRequest();
        invalidCancel.setStatus(OrderStatus.CANCELLED);
        Long deliveredOrderId = order.getId();
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrderStatus(deliveredOrderId, invalidCancel);
        }, "Transition from DELIVERED to CANCELLED must be rejected");

        // 3. DELIVERED order cannot transition back to ACCEPTED or PREPARING
        OrderStatusUpdateRequest invalidReopen = new OrderStatusUpdateRequest();
        invalidReopen.setStatus(OrderStatus.ACCEPTED);
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrderStatus(deliveredOrderId, invalidReopen);
        }, "Transition from DELIVERED to ACCEPTED must be rejected");
    }

    @Test
    @DisplayName("Test 5: Idempotency key prevents duplicate order submissions")
    public void testIdempotencyKeyPreventsDuplicateSubmission() {
        assertNotNull(storeShivajinagar);

        String idempotencyKey = "IDEMP-" + UUID.randomUUID();

        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(testCustomer.getId());
        req.setStoreId(storeShivajinagar.getId());
        req.setDeliveryAddress("Shivajinagar, Pune");
        req.setIdempotencyKey(idempotencyKey);
        req.setItems(List.of(new OrderItemRequest(testProduct.getId(), 1)));

        // Initial submission
        Order order1 = orderService.createOrder(req);
        assertNotNull(order1);

        // Immediate duplicate submission with the same idempotency key
        Order order2 = orderService.createOrder(req);
        assertNotNull(order2);

        assertEquals(order1.getId(), order2.getId(), "Both calls must return the identical existing order");
        assertEquals(order1.getOrderNumber(), order2.getOrderNumber(), "Order numbers must match");
    }

    @Test
    @DisplayName("Test 6: Order rejection by Store Owner releases inventory and cancels order")
    public void testStoreOwnerOrderRejectionReleasesInventory() {
        assertNotNull(storeShivajinagar);
        assertNotNull(testProduct);

        StoreInventory inv = inventoryRepository.findByStoreIdAndProductId(storeShivajinagar.getId(), testProduct.getId()).get();
        inv.setStockQuantity(30);
        inventoryRepository.save(inv);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(testCustomer.getId());
        req.setStoreId(storeShivajinagar.getId());
        req.setDeliveryAddress("Shivajinagar, Pune");
        req.setItems(List.of(new OrderItemRequest(testProduct.getId(), 5)));

        Order order = orderService.createOrder(req);

        // Stock decreased from 30 to 25
        StoreInventory afterOrderInv = inventoryRepository.findByStoreIdAndProductId(storeShivajinagar.getId(), testProduct.getId()).get();
        assertEquals(25, afterOrderInv.getStockQuantity());

        // Store owner rejects order
        Order rejectedOrder = orderService.rejectOrder(order.getId(), "Item damaged in batch", testStoreOwner.getId());

        assertEquals(OrderStatus.CANCELLED, rejectedOrder.getStatus());
        assertTrue(rejectedOrder.getCancellationReason().contains("Rejected by store"));

        // Stock restored back to 30
        StoreInventory afterRejectInv = inventoryRepository.findByStoreIdAndProductId(storeShivajinagar.getId(), testProduct.getId()).get();
        assertEquals(30, afterRejectInv.getStockQuantity(), "Inventory must be restored upon rejection");
    }
}
