package com.rucart.backend.service;

import com.rucart.backend.dto.OrderItemRequest;
import com.rucart.backend.dto.StoreAssignmentResult;
import com.rucart.backend.model.Product;
import com.rucart.backend.model.Store;
import com.rucart.backend.model.StoreInventory;
import com.rucart.backend.repository.ProductRepository;
import com.rucart.backend.repository.StoreInventoryRepository;
import com.rucart.backend.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StoreAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(StoreAssignmentService.class);

    private final StoreRepository storeRepository;
    private final StoreInventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public StoreAssignmentService(StoreRepository storeRepository,
                                  StoreInventoryRepository inventoryRepository,
                                  ProductRepository productRepository) {
        this.storeRepository = storeRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Assigns the most suitable operating store based on service area, operating status,
     * product availability, and inventory depth.
     */
    public StoreAssignmentResult assignStoreForOrder(String deliveryArea, List<OrderItemRequest> items) {
        // 1. Fetch all operating stores (active, approved, and currently open)
        List<Store> operatingStores = storeRepository.findByApprovedTrueAndActiveTrueAndOpenTrue();

        if (operatingStores.isEmpty()) {
            return new StoreAssignmentResult(null, deliveryArea, false,
                    "No grocery stores are currently operating or open in your area. Please check store operating hours.");
        }

        // 2. Filter operating stores that service this area
        List<Store> candidateStores;
        if (deliveryArea != null && !deliveryArea.trim().isEmpty()) {
            candidateStores = operatingStores.stream()
                    .filter(store -> store.servesArea(deliveryArea))
                    .collect(Collectors.toList());

            if (candidateStores.isEmpty()) {
                // If no store matches exact locality, fallback to stores serving the same city
                candidateStores = operatingStores;
            }
        } else {
            candidateStores = operatingStores;
        }

        StoreAssignmentResult result = new StoreAssignmentResult();
        result.setDeliveryArea(deliveryArea != null ? deliveryArea : "Default Area");
        result.setCandidateStoresEvaluated(candidateStores.size());

        if (items == null || items.isEmpty()) {
            // Cart has no items, assign top candidate store
            Store defaultStore = candidateStores.get(0);
            result.setAssignedStore(defaultStore);
            result.setCanFulfill(true);
            result.setMessage("Assigned store: " + defaultStore.getName());
            return result;
        }

        // 3. Evaluate each candidate store for inventory fulfillment & compute score
        Store bestStore = null;
        double bestScore = -1.0;
        List<String> lastDiagnosticFailures = new ArrayList<>();

        for (Store store : candidateStores) {
            boolean allAvailable = true;
            double score = 0.0;
            List<String> storeFailures = new ArrayList<>();

            // Priority 1: Direct locality match in service areas or address
            if (deliveryArea != null && store.servesArea(deliveryArea)) {
                score += 200.0;
            }
            if (deliveryArea != null && store.getAddress() != null &&
                    store.getAddress().toLowerCase().contains(deliveryArea.toLowerCase().trim())) {
                score += 100.0;
            }

            // Priority 2: Store rating
            if (store.getRating() != null) {
                score += store.getRating() * 10.0;
            }

            for (OrderItemRequest item : items) {
                Optional<StoreInventory> invOpt = inventoryRepository.findByStoreIdAndProductId(store.getId(), item.getProductId());
                if (invOpt.isEmpty()) {
                    allAvailable = false;
                    String pName = getProductName(item.getProductId());
                    storeFailures.add(pName + " is not stocked at " + store.getName());
                    continue;
                }

                StoreInventory inv = invOpt.get();
                if (!inv.isAvailable()) {
                    allAvailable = false;
                    String pName = getProductName(item.getProductId());
                    storeFailures.add(pName + " is marked unavailable at " + store.getName());
                    continue;
                }

                if (inv.getStockQuantity() < item.getQuantity()) {
                    allAvailable = false;
                    String pName = getProductName(item.getProductId());
                    storeFailures.add(pName + " has only " + inv.getStockQuantity() + " units available at " + store.getName() + " (requested: " + item.getQuantity() + ")");
                    continue;
                }

                // Add inventory surplus score to prioritize well-stocked stores
                score += (inv.getStockQuantity() - item.getQuantity());
            }

            if (allAvailable) {
                if (score > bestScore) {
                    bestScore = score;
                    bestStore = store;
                }
            } else {
                lastDiagnosticFailures.addAll(storeFailures);
            }
        }

        if (bestStore != null) {
            result.setAssignedStore(bestStore);
            result.setCanFulfill(true);
            result.setMessage("Order assigned to " + bestStore.getName() + " based on product availability and area coverage.");
            return result;
        }

        // Could not fulfill completely with any single operating store
        result.setAssignedStore(null);
        result.setCanFulfill(false);
        result.setUnfulfillableItems(lastDiagnosticFailures);
        result.setMessage("Selected items cannot be fulfilled by any open store in " + (deliveryArea != null ? deliveryArea : "your location") + ".");
        return result;
    }

    /**
     * Lists all supported service areas across operating stores.
     */
    public List<String> getSupportedServiceAreas() {
        Set<String> areas = new LinkedHashSet<>();
        // Add common Pune areas
        areas.add("Shivajinagar");
        areas.add("FC Road");
        areas.add("Kothrud");
        areas.add("Paud Road");
        areas.add("Deccan Gymkhana");
        areas.add("Viman Nagar");
        areas.add("Kalyani Nagar");
        areas.add("Hinjewadi");
        areas.add("Baner");
        areas.add("Aundh");
        areas.add("Wakad");
        areas.add("Hadapsar");

        List<Store> stores = storeRepository.findAll();
        for (Store s : stores) {
            if (s.getServiceAreas() != null) {
                String[] parts = s.getServiceAreas().split("[,;]");
                for (String p : parts) {
                    String trimmed = p.trim();
                    if (!trimmed.isEmpty()) {
                        areas.add(trimmed);
                    }
                }
            }
        }
        return new ArrayList<>(areas);
    }

    private String getProductName(Long productId) {
        return productRepository.findById(productId)
                .map(Product::getName)
                .orElse("Product #" + productId);
    }
}
