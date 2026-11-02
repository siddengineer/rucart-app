package com.rucart.backend.service;

import com.rucart.backend.dto.ProductCreateRequest;
import com.rucart.backend.dto.ProductUpdateRequest;
import com.rucart.backend.dto.StoreProductDto;
import com.rucart.backend.model.*;
import com.rucart.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final StoreInventoryRepository inventoryRepository;
    private final StockAdjustmentHistoryRepository historyRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          StoreRepository storeRepository,
                          StoreInventoryRepository inventoryRepository,
                          StockAdjustmentHistoryRepository historyRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
        this.inventoryRepository = inventoryRepository;
        this.historyRepository = historyRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    public List<Product> getAllAvailableProducts() {
        return productRepository.findByAvailableTrue();
    }

    public List<Product> getAllProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndAvailableTrue(categoryId);
    }

    public List<Product> getProductsByStore(Long storeId) {
        return productRepository.findByStoreIdAndAvailableTrue(storeId);
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllAvailableProducts();
        }
        return productRepository.findByNameContainingIgnoreCaseAndAvailableTrue(query.trim());
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Transactional
    public Product createProduct(ProductCreateRequest request, User creator) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        Store store = null;
        if (request.getStoreId() != null) {
            store = storeRepository.findById(request.getStoreId()).orElse(null);
        }
        if (store == null) {
            // Default to primary flagship store
            store = storeRepository.findAll().stream().findFirst().orElse(null);
        }

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice() != null ? request.getOriginalPrice() : request.getPrice());
        product.setUnit(request.getUnit().trim());
        product.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : "ic_cart");
        product.setBrand(request.getBrand() != null ? request.getBrand().trim() : "Rucart Fresh");
        product.setCategory(category);
        product.setStore(store);
        product.setStock(request.getInitialStock() != null ? request.getInitialStock() : 50);
        product.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        // Automatically create StoreInventory entry for the store
        if (store != null) {
            StoreInventory inventory = new StoreInventory();
            inventory.setStore(store);
            inventory.setProduct(savedProduct);
            inventory.setPrice(savedProduct.getPrice());
            int stock = request.getInitialStock() != null ? request.getInitialStock() : 50;
            inventory.setStockQuantity(stock);
            int threshold = request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10;
            inventory.setLowStockThreshold(threshold);
            inventory.setAvailable(savedProduct.isAvailable() && stock > 0);
            inventory.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);

            // Record initial stock adjustment history
            StockAdjustmentHistory history = new StockAdjustmentHistory(
                    store,
                    savedProduct,
                    0,
                    stock,
                    StockAdjustmentType.INITIAL_STOCK,
                    "Initial product catalog creation",
                    creator
            );
            historyRepository.save(history);
        }

        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Long id, ProductUpdateRequest request) {
        Product product = getProductById(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getOriginalPrice() != null) {
            product.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getUnit() != null && !request.getUnit().isBlank()) {
            product.setUnit(request.getUnit().trim());
        }
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            product.setImageUrl(request.getImageUrl().trim());
        }
        if (request.getBrand() != null && !request.getBrand().isBlank()) {
            product.setBrand(request.getBrand().trim());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }
        if (request.getAvailable() != null) {
            product.setAvailable(request.getAvailable());
        }
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    @Transactional
    public Product setProductStatus(Long id, boolean available) {
        Product product = getProductById(id);
        product.setAvailable(available);
        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);

        // Update all store inventories for this product
        List<StoreInventory> inventories = inventoryRepository.findByProductId(id);
        for (StoreInventory inv : inventories) {
            inv.setAvailable(available && inv.getStockQuantity() > 0);
            inv.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inv);
        }

        return saved;
    }

    public List<StoreProductDto> getStoreProducts(Long storeId, Long categoryId, String search) {
        List<StoreInventory> inventories;
        if (storeId != null) {
            inventories = inventoryRepository.findByStoreId(storeId);
        } else {
            inventories = inventoryRepository.findAll();
        }

        List<StoreProductDto> result = new ArrayList<>();
        for (StoreInventory inv : inventories) {
            Product p = inv.getProduct();

            // Filter out deactivated products
            if (!p.isAvailable()) {
                continue;
            }

            // Category filter
            if (categoryId != null && (p.getCategory() == null || !p.getCategory().getId().equals(categoryId))) {
                continue;
            }

            // Search filter
            if (search != null && !search.trim().isEmpty()) {
                String q = search.trim().toLowerCase();
                boolean matchesName = p.getName() != null && p.getName().toLowerCase().contains(q);
                boolean matchesDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(q);
                boolean matchesCategory = p.getCategory() != null && p.getCategory().getName().toLowerCase().contains(q);
                if (!matchesName && !matchesDesc && !matchesCategory) {
                    continue;
                }
            }

            StoreProductDto dto = new StoreProductDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setPrice(inv.getPrice() != null ? inv.getPrice() : p.getPrice());
            dto.setOriginalPrice(p.getOriginalPrice() != null ? p.getOriginalPrice() : dto.getPrice());
            dto.setUnit(p.getUnit());
            dto.setImageUrl(p.getImageUrl());
            dto.setBrand(p.getBrand());
            if (p.getCategory() != null) {
                dto.setCategoryId(p.getCategory().getId());
                dto.setCategoryName(p.getCategory().getName());
            }
            if (inv.getStore() != null) {
                dto.setStoreId(inv.getStore().getId());
                dto.setStoreName(inv.getStore().getName());
            }
            dto.setStockQuantity(inv.getStockQuantity());
            dto.setLowStockThreshold(inv.getLowStockThreshold());
            dto.setAvailable(inv.isAvailable() && p.isAvailable() && inv.getStockQuantity() > 0);
            dto.setLowStock(inv.isLowStock());

            result.add(dto);
        }

        return result;
    }
}
