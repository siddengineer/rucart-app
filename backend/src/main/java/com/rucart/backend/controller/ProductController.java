package com.rucart.backend.controller;

import com.rucart.backend.dto.ApiResponse;
import com.rucart.backend.dto.ProductCreateRequest;
import com.rucart.backend.dto.ProductUpdateRequest;
import com.rucart.backend.dto.StoreProductDto;
import com.rucart.backend.model.Product;
import com.rucart.backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String search) {

        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search);
        } else if (categoryId != null) {
            products = productService.getProductsByCategory(categoryId);
        } else if (storeId != null) {
            products = productService.getProductsByStore(storeId);
        } else {
            products = productService.getAllAvailableProducts();
        }
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProductsForAdmin() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getAllProducts()));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<StoreProductDto>>> getStoreProducts(
            @PathVariable Long storeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {

        List<StoreProductDto> products = productService.getStoreProducts(storeId, categoryId, search);
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        Product created = productService.createProduct(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Product created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        Product updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Product>> toggleProductStatus(
            @PathVariable Long id,
            @RequestParam boolean available) {
        Product updated = productService.setProductStatus(id, available);
        String msg = available ? "Product activated successfully" : "Product deactivated successfully";
        return ResponseEntity.ok(ApiResponse.ok(msg, updated));
    }
}
