package com.rucart.backend.repository;

import com.rucart.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryIdAndAvailableTrue(Long categoryId);

    List<Product> findByStoreIdAndAvailableTrue(Long storeId);

    List<Product> findByNameContainingIgnoreCaseAndAvailableTrue(String name);

    List<Product> findByAvailableTrue();

    List<Product> findAllByOrderByCreatedAtDesc();

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByStoreId(Long storeId);
}
