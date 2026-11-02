package com.rucart.backend;

import com.rucart.backend.dto.*;
import com.rucart.backend.model.*;
import com.rucart.backend.repository.*;
import com.rucart.backend.service.OrderService;
import com.rucart.backend.service.ProductService;
import com.rucart.backend.service.StoreInventoryService;
import com.rucart.backend.util.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductAndInventoryIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private StoreInventoryService inventoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreInventoryRepository inventoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testCustomer;
    private Store testStore;
    private Category testCategory;

    @BeforeEach
    public void setup() {
        testCustomer = userRepository.findByPhone("9876543210").orElse(null);
        if (testCustomer == null) {
            testCustomer = new User("Test Customer", "9876543210", "cust@test.com", "pass", Role.CUSTOMER, "Shivajinagar, Pune");
            testCustomer = userRepository.save(testCustomer);
        }

        for (Store s : storeRepository.findAll()) {
            s.setOpen(true);
            s.setActive(true);
            storeRepository.save(s);
        }

        testStore = storeRepository.findAll().stream().findFirst().orElse(null);
        if (testStore == null) {
            testStore = new Store("Flagship Test Store", "Shivajinagar, Pune", "020-123456", "test@store.com", "09:00 - 22:00", testCustomer, "ic_store");
            testStore = storeRepository.save(testStore);
        }

        testCategory = categoryRepository.findAll().stream().findFirst().orElse(null);
        if (testCategory == null) {
            testCategory = new Category("Fresh Veggies", "Organic and fresh vegetables", "ic_vegetables", "ic_vegetables", 1);
            testCategory = categoryRepository.save(testCategory);
        }
    }

    @Test
    @DisplayName("1. Product creation, editing, and deactivation lifecycle")
    public void testProductLifecycle() {
        ProductCreateRequest createReq = new ProductCreateRequest();
        createReq.setName("Organic Broccoli");
        createReq.setDescription("Fresh farm broccoli");
        createReq.setPrice(new BigDecimal("65.00"));
        createReq.setOriginalPrice(new BigDecimal("80.00"));
        createReq.setUnit("500 g");
        createReq.setImageUrl("ic_vegetables");
        createReq.setBrand("Rucart Organic");
        createReq.setCategoryId(testCategory.getId());
        createReq.setStoreId(testStore.getId());
        createReq.setInitialStock(40);
        createReq.setLowStockThreshold(8);
        createReq.setAvailable(true);

        Product created = productService.createProduct(createReq, testCustomer);
        assertNotNull(created.getId());
        assertEquals("Organic Broccoli", created.getName());
        assertTrue(created.isAvailable());

        // Verify initial store inventory entry was created
        StoreInventory inv = inventoryRepository.findByStoreIdAndProductId(testStore.getId(), created.getId()).orElse(null);
        assertNotNull(inv);
        assertEquals(40, inv.getStockQuantity());
        assertEquals(8, inv.getLowStockThreshold());

        // Edit product
        ProductUpdateRequest updateReq = new ProductUpdateRequest();
        updateReq.setName("Organic Green Broccoli");
        updateReq.setPrice(new BigDecimal("70.00"));
        Product updated = productService.updateProduct(created.getId(), updateReq);
        assertEquals("Organic Green Broccoli", updated.getName());
        assertEquals(new BigDecimal("70.00"), updated.getPrice());

        // Deactivate product
        Product deactivated = productService.setProductStatus(created.getId(), false);
        assertFalse(deactivated.isAvailable());

        // Store inventory should also reflect unavailable
        StoreInventory deactInv = inventoryRepository.findByStoreIdAndProductId(testStore.getId(), created.getId()).orElse(null);
        assertNotNull(deactInv);
        assertFalse(deactInv.isAvailable());

        // Reactivate product
        Product reactivated = productService.setProductStatus(created.getId(), true);
        assertTrue(reactivated.isAvailable());
    }

    @Test
    @DisplayName("2. Stock adjustment, history audit log, and low-stock alerts")
    public void testStockAdjustmentAndAlerts() {
        // Create product
        ProductCreateRequest createReq = new ProductCreateRequest();
        createReq.setName("Red Bell Pepper");
        createReq.setPrice(new BigDecimal("45.00"));
        createReq.setUnit("250 g");
        createReq.setCategoryId(testCategory.getId());
        createReq.setStoreId(testStore.getId());
        createReq.setInitialStock(25);
        createReq.setLowStockThreshold(10);
        Product p = productService.createProduct(createReq, testCustomer);

        // Adjust stock down to 5 (below threshold 10)
        StockAdjustmentRequest adjReq = new StockAdjustmentRequest();
        adjReq.setProductId(p.getId());
        adjReq.setNewQuantity(5);
        adjReq.setReason("Spillage and damage inspection");
        inventoryService.adjustStockManually(testStore.getId(), adjReq, testCustomer);

        // Verify inventory
        StoreInventory inv = inventoryRepository.findByStoreIdAndProductId(testStore.getId(), p.getId()).orElseThrow();
        assertEquals(5, inv.getStockQuantity());
        assertTrue(inv.isLowStock());

        // Verify low stock alert appears
        List<LowStockAlertDto> alerts = inventoryService.getLowStockAlerts(testStore.getId());
        boolean alertFound = alerts.stream().anyMatch(a -> a.getProductId().equals(p.getId()) && a.getCurrentStock() == 5);
        assertTrue(alertFound, "Product should appear in low-stock alerts list");

        // Verify stock adjustment history recorded
        List<StockAdjustmentHistoryDto> history = inventoryService.getStockAdjustmentHistory(testStore.getId(), p.getId());
        assertFalse(history.isEmpty());
        assertEquals(StockAdjustmentType.MANUAL_ADJUSTMENT, history.get(0).getAdjustmentType());
        assertEquals("Spillage and damage inspection", history.get(0).getReason());
    }

    @Test
    @DisplayName("3. Inventory validation before checkout")
    public void testInventoryValidation() {
        // Create product with stock 10
        ProductCreateRequest createReq = new ProductCreateRequest();
        createReq.setName("Sweet Corn Pack");
        createReq.setPrice(new BigDecimal("35.00"));
        createReq.setUnit("2 pcs");
        createReq.setCategoryId(testCategory.getId());
        createReq.setStoreId(testStore.getId());
        createReq.setInitialStock(10);
        Product p = productService.createProduct(createReq, testCustomer);

        // Case A: valid quantity
        InventoryValidationRequest validReq = new InventoryValidationRequest(
                testStore.getId(),
                List.of(new OrderItemRequest(p.getId(), 5))
        );
        InventoryValidationResponse resA = inventoryService.validateInventory(validReq);
        assertTrue(resA.isValid());

        // Case B: exceeding quantity (15 requested, 10 available)
        InventoryValidationRequest invalidReq = new InventoryValidationRequest(
                testStore.getId(),
                List.of(new OrderItemRequest(p.getId(), 15))
        );
        InventoryValidationResponse resB = inventoryService.validateInventory(invalidReq);
        assertFalse(resB.isValid());
        assertEquals(10, resB.getItems().get(0).getAvailableQuantity());
        assertFalse(resB.getItems().get(0).getAvailable());
    }

    @Test
    @DisplayName("4. Stock reservation on order placement and stock release on cancellation")
    public void testOrderReservationAndCancellation() {
        ProductCreateRequest createReq = new ProductCreateRequest();
        createReq.setName("Fresh Cauliflower");
        createReq.setPrice(new BigDecimal("30.00"));
        createReq.setUnit("1 pc");
        createReq.setCategoryId(testCategory.getId());
        createReq.setStoreId(testStore.getId());
        createReq.setInitialStock(20);
        Product p = productService.createProduct(createReq, testCustomer);

        // Place order for 6 items
        OrderCreateRequest orderReq = new OrderCreateRequest();
        orderReq.setCustomerId(testCustomer.getId());
        orderReq.setStoreId(testStore.getId());
        orderReq.setDeliveryAddress("Flat 101, Test Residency, Pune");
        orderReq.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        orderReq.setItems(List.of(new OrderItemRequest(p.getId(), 6)));

        Order order = orderService.createOrder(orderReq);
        assertNotNull(order.getId());

        // Stock should now be 20 - 6 = 14
        StoreInventory afterOrder = inventoryRepository.findByStoreIdAndProductId(testStore.getId(), p.getId()).orElseThrow();
        assertEquals(14, afterOrder.getStockQuantity());

        // Cancel order -> Stock should be restored to 20
        Order cancelledOrder = orderService.cancelOrder(order.getId(), "Customer changed plan", testCustomer.getId());
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());

        StoreInventory afterCancel = inventoryRepository.findByStoreIdAndProductId(testStore.getId(), p.getId()).orElseThrow();
        assertEquals(20, afterCancel.getStockQuantity());
    }

    @Test
    @DisplayName("5. Protection against overselling when requested quantity exceeds available stock")
    public void testOversellingPrevention() {
        ProductCreateRequest createReq = new ProductCreateRequest();
        createReq.setName("Imported Dragon Fruit");
        createReq.setPrice(new BigDecimal("120.00"));
        createReq.setUnit("1 pc");
        createReq.setCategoryId(testCategory.getId());
        createReq.setStoreId(testStore.getId());
        createReq.setInitialStock(3);
        Product p = productService.createProduct(createReq, testCustomer);

        // Attempt to order 5 units when only 3 exist
        OrderCreateRequest orderReq = new OrderCreateRequest();
        orderReq.setCustomerId(testCustomer.getId());
        orderReq.setStoreId(testStore.getId());
        orderReq.setDeliveryAddress("Shivajinagar, Pune");
        orderReq.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        orderReq.setItems(List.of(new OrderItemRequest(p.getId(), 5)));

        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(orderReq);
        });

        // Verify stock remains untouched at 3
        StoreInventory inv = inventoryRepository.findByStoreIdAndProductId(testStore.getId(), p.getId()).orElseThrow();
        assertEquals(3, inv.getStockQuantity());
    }

    @Test
    @DisplayName("6. Protection against overselling with concurrent order attempts (Pessimistic Locking)")
    public void testConcurrentOrdersProtection() throws InterruptedException {
        // Setup item with EXACTLY 5 in stock
        ProductCreateRequest createReq = new ProductCreateRequest();
        createReq.setName("Limited Stock Alphonso Mangoes");
        createReq.setPrice(new BigDecimal("500.00"));
        createReq.setUnit("1 dozen");
        createReq.setCategoryId(testCategory.getId());
        createReq.setStoreId(testStore.getId());
        createReq.setInitialStock(5);
        Product p = productService.createProduct(createReq, testCustomer);

        // Launch 10 simultaneous threads each attempting to order 1 unit (Total requested: 10, Available: 5)
        int threadsCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadsCount);

        AtomicInteger successfulOrders = new AtomicInteger(0);
        AtomicInteger failedOrders = new AtomicInteger(0);

        for (int i = 0; i < threadsCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // ensure all threads fire at the exact same instant
                    OrderCreateRequest req = new OrderCreateRequest();
                    req.setCustomerId(testCustomer.getId());
                    req.setStoreId(testStore.getId());
                    req.setDeliveryAddress("Pune Concurrent Test");
                    req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
                    req.setItems(List.of(new OrderItemRequest(p.getId(), 1)));

                    orderService.createOrder(req);
                    successfulOrders.incrementAndGet();
                } catch (Exception ex) {
                    failedOrders.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Release the gates simultaneously
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All concurrent tasks should complete within timeout");

        // Exactly 5 orders must have succeeded, and exactly 5 must have failed!
        assertEquals(5, successfulOrders.get(), "Exactly 5 orders should succeed for 5 stock items");
        assertEquals(5, failedOrders.get(), "5 concurrent orders should fail due to stock depletion");

        // Final remaining stock in database must be exactly 0, NEVER negative
        StoreInventory finalInv = inventoryRepository.findByStoreIdAndProductId(testStore.getId(), p.getId()).orElseThrow();
        assertEquals(0, finalInv.getStockQuantity(), "Stock must reach exactly 0 without overselling");
        assertFalse(finalInv.isAvailable(), "Inventory must be marked unavailable when stock hits 0");
    }
}
