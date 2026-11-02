package com.rucart.backend.service;

import com.rucart.backend.dto.*;
import com.rucart.backend.model.*;
import com.rucart.backend.repository.*;
import com.rucart.backend.util.InsufficientStockException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StoreInventoryService {

    private final StoreInventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final StockAdjustmentHistoryRepository historyRepository;
    private final NotificationService notificationService;

    @PersistenceContext
    private EntityManager entityManager;

    public StoreInventoryService(StoreInventoryRepository inventoryRepository,
                                 StoreRepository storeRepository,
                                 ProductRepository productRepository,
                                 StockAdjustmentHistoryRepository historyRepository,
                                 NotificationService notificationService) {
        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
    }

    public List<StoreInventory> getStoreInventory(Long storeId) {
        return inventoryRepository.findByStoreId(storeId);
    }

    public List<StoreInventory> getAvailableStoreInventory(Long storeId) {
        return inventoryRepository.findByStoreIdAndAvailableTrue(storeId);
    }

    @Transactional
    public StoreInventory setInventoryItem(Long storeId, StoreInventoryRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));

        Optional<StoreInventory> existingOpt = inventoryRepository.findByStoreIdAndProductId(storeId, request.getProductId());
        StoreInventory inventory;
        int previousStock = 0;
        if (existingOpt.isPresent()) {
            inventory = existingOpt.get();
            previousStock = inventory.getStockQuantity();
        } else {
            inventory = new StoreInventory();
            inventory.setStore(store);
            inventory.setProduct(product);
        }

        inventory.setPrice(request.getPrice() != null ? request.getPrice() : product.getPrice());
        int newStock = request.getStockQuantity() != null ? request.getStockQuantity() : 50;
        inventory.setStockQuantity(newStock);
        inventory.setLowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10);
        inventory.setAvailable(request.getAvailable() != null ? request.getAvailable() : (newStock > 0));
        inventory.setUpdatedAt(LocalDateTime.now());

        StoreInventory saved = inventoryRepository.save(inventory);

        // Record history log
        StockAdjustmentHistory history = new StockAdjustmentHistory(
                store,
                product,
                previousStock,
                newStock,
                StockAdjustmentType.MANUAL_ADJUSTMENT,
                "Store inventory updated",
                null
        );
        historyRepository.save(history);

        return saved;
    }

    /**
     * Pre-checkout validation of requested items against live store inventory.
     */
    public InventoryValidationResponse validateInventory(InventoryValidationRequest request) {
        Long storeId = request.getStoreId();
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

        boolean overallValid = true;
        List<InventoryItemValidationDto> itemResults = new ArrayList<>();

        for (OrderItemRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product == null || !product.isAvailable()) {
                overallValid = false;
                itemResults.add(new InventoryItemValidationDto(
                        item.getProductId(),
                        product != null ? product.getName() : "Unknown Product",
                        item.getQuantity(),
                        0,
                        false,
                        null,
                        "Product is currently unavailable or deactivated."
                ));
                continue;
            }

            Optional<StoreInventory> invOpt = inventoryRepository.findByStoreIdAndProductId(storeId, item.getProductId());
            if (invOpt.isEmpty()) {
                overallValid = false;
                itemResults.add(new InventoryItemValidationDto(
                        product.getId(),
                        product.getName(),
                        item.getQuantity(),
                        0,
                        false,
                        product.getPrice(),
                        "Not stocked at this store."
                ));
                continue;
            }

            StoreInventory inv = invOpt.get();
            boolean itemAvailable = inv.isAvailable() && inv.getStockQuantity() >= item.getQuantity();
            if (!itemAvailable) {
                overallValid = false;
                String msg = inv.getStockQuantity() <= 0
                        ? "Out of stock."
                        : String.format("Only %d units available (requested %d).", inv.getStockQuantity(), item.getQuantity());

                itemResults.add(new InventoryItemValidationDto(
                        product.getId(),
                        product.getName(),
                        item.getQuantity(),
                        inv.getStockQuantity(),
                        false,
                        inv.getPrice(),
                        msg
                ));
            } else {
                itemResults.add(new InventoryItemValidationDto(
                        product.getId(),
                        product.getName(),
                        item.getQuantity(),
                        inv.getStockQuantity(),
                        true,
                        inv.getPrice(),
                        "In stock and available."
                ));
            }
        }

        String summary = overallValid
                ? "All items are in stock and reserved for checkout."
                : "One or more items are unavailable or exceed current stock quantities.";

        return new InventoryValidationResponse(overallValid, summary, itemResults);
    }

    /**
     * Pessimistic locking reservation of stock when an order is placed.
     * Prevents concurrent overselling.
     */
    @Transactional
    public StoreInventory reserveStockWithLock(Long storeId, Long productId, int quantity, String orderNumber, User customer) {
        StoreInventory inventory = inventoryRepository.findByStoreIdAndProductIdWithLock(storeId, productId)
                .orElseThrow(() -> new RuntimeException("Product " + productId + " not found in store " + storeId + " inventory"));
        entityManager.refresh(inventory, LockModeType.PESSIMISTIC_WRITE);

        Product product = inventory.getProduct();
        if (!product.isAvailable() || !inventory.isAvailable()) {
            throw new RuntimeException("Product '" + product.getName() + "' is currently marked as unavailable");
        }

        int currentStock = inventory.getStockQuantity();
        if (currentStock < quantity) {
            throw new InsufficientStockException(productId, product.getName(), currentStock, quantity);
        }

        int newStock = currentStock - quantity;
        inventory.setStockQuantity(newStock);
        if (newStock == 0) {
            inventory.setAvailable(false);
        }
        inventory.setUpdatedAt(LocalDateTime.now());
        StoreInventory saved = inventoryRepository.save(inventory);

        // Record stock adjustment audit history
        StockAdjustmentHistory history = new StockAdjustmentHistory(
                inventory.getStore(),
                product,
                currentStock,
                newStock,
                StockAdjustmentType.ORDER_RESERVATION,
                "Reserved for Order #" + orderNumber,
                customer
        );
        historyRepository.save(history);

        // Check if low stock threshold reached -> send alert notification to store owner
        if (saved.isLowStock() && inventory.getStore().getOwner() != null) {
            notificationService.sendNotification(
                    inventory.getStore().getOwner().getId(),
                    "Low Stock Alert: " + product.getName(),
                    String.format("Stock for '%s' dropped to %d (Threshold: %d) at %s",
                            product.getName(), newStock, saved.getLowStockThreshold(), inventory.getStore().getName()),
                    NotificationType.STORE_ALERT,
                    "PROD-" + product.getId()
            );
        }

        return saved;
    }

    /**
     * Pessimistic locking stock release when an order is cancelled.
     * Restores the reserved quantity back to available store inventory.
     */
    @Transactional
    public StoreInventory releaseStockWithLock(Long storeId, Long productId, int quantity, String orderNumber, User user, String reason) {
        StoreInventory inventory = inventoryRepository.findByStoreIdAndProductIdWithLock(storeId, productId)
                .orElseThrow(() -> new RuntimeException("Product " + productId + " not found in store " + storeId + " inventory"));

        int currentStock = inventory.getStockQuantity();
        int newStock = currentStock + quantity;
        inventory.setStockQuantity(newStock);
        if (newStock > 0) {
            inventory.setAvailable(true);
        }
        inventory.setUpdatedAt(LocalDateTime.now());
        StoreInventory saved = inventoryRepository.save(inventory);

        // Record stock adjustment history
        String historyReason = "Restocked from Cancelled Order #" + orderNumber + (reason != null && !reason.isBlank() ? " (" + reason + ")" : "");
        StockAdjustmentHistory history = new StockAdjustmentHistory(
                inventory.getStore(),
                inventory.getProduct(),
                currentStock,
                newStock,
                StockAdjustmentType.ORDER_CANCELLATION_RESTOCK,
                historyReason,
                user
        );
        historyRepository.save(history);

        return saved;
    }

    /**
     * Manual stock adjustment by store owner or admin.
     */
    @Transactional
    public StoreInventory adjustStockManually(Long storeId, StockAdjustmentRequest request, User adjustedBy) {
        StoreInventory inventory = inventoryRepository.findByStoreIdAndProductIdWithLock(storeId, request.getProductId())
                .orElseThrow(() -> new RuntimeException("Store inventory record not found for product ID: " + request.getProductId()));

        int previousStock = inventory.getStockQuantity();
        int newStock = previousStock;

        if (request.getNewQuantity() != null) {
            newStock = Math.max(0, request.getNewQuantity());
        } else if (request.getChangeAmount() != null) {
            newStock = Math.max(0, previousStock + request.getChangeAmount());
        }

        inventory.setStockQuantity(newStock);
        if (request.getNewPrice() != null) {
            inventory.setPrice(request.getNewPrice());
        }
        if (request.getLowStockThreshold() != null) {
            inventory.setLowStockThreshold(request.getLowStockThreshold());
        }
        if (request.getAvailable() != null) {
            inventory.setAvailable(request.getAvailable());
        } else {
            inventory.setAvailable(newStock > 0);
        }
        inventory.setUpdatedAt(LocalDateTime.now());

        StoreInventory saved = inventoryRepository.save(inventory);

        String reason = request.getReason() != null && !request.getReason().isBlank()
                ? request.getReason()
                : "Manual stock adjustment by " + (adjustedBy != null ? adjustedBy.getName() : "Store Owner");

        StockAdjustmentHistory history = new StockAdjustmentHistory(
                inventory.getStore(),
                inventory.getProduct(),
                previousStock,
                newStock,
                StockAdjustmentType.MANUAL_ADJUSTMENT,
                reason,
                adjustedBy
        );
        historyRepository.save(history);

        return saved;
    }

    /**
     * Get low stock alerts for a given store.
     */
    public List<LowStockAlertDto> getLowStockAlerts(Long storeId) {
        List<StoreInventory> lowList;
        if (storeId != null) {
            lowList = inventoryRepository.findLowStockInventories(storeId);
        } else {
            lowList = inventoryRepository.findAllLowStockInventories();
        }

        return lowList.stream().map(inv -> new LowStockAlertDto(
                inv.getStore().getId(),
                inv.getStore().getName(),
                inv.getProduct().getId(),
                inv.getProduct().getName(),
                inv.getProduct().getCategory() != null ? inv.getProduct().getCategory().getName() : "General",
                inv.getProduct().getUnit(),
                inv.getPrice(),
                inv.getStockQuantity(),
                inv.getLowStockThreshold()
        )).collect(Collectors.toList());
    }

    /**
     * Get stock adjustment history audit log.
     */
    public List<StockAdjustmentHistoryDto> getStockAdjustmentHistory(Long storeId, Long productId) {
        List<StockAdjustmentHistory> list;
        if (storeId != null && productId != null) {
            list = historyRepository.findByStoreIdAndProductIdOrderByCreatedAtDesc(storeId, productId);
        } else if (storeId != null) {
            list = historyRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
        } else if (productId != null) {
            list = historyRepository.findByProductIdOrderByCreatedAtDesc(productId);
        } else {
            list = historyRepository.findAll();
        }

        return list.stream().map(h -> new StockAdjustmentHistoryDto(
                h.getId(),
                h.getStore().getId(),
                h.getStore().getName(),
                h.getProduct().getId(),
                h.getProduct().getName(),
                h.getPreviousQuantity(),
                h.getNewQuantity(),
                h.getQuantityChange(),
                h.getAdjustmentType(),
                h.getReason(),
                h.getAdjustedBy() != null ? h.getAdjustedBy().getName() : "System Automation",
                h.getCreatedAt()
        )).collect(Collectors.toList());
    }
}
