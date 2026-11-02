package com.rucart.backend.repository;

import com.rucart.backend.model.StoreInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {

    List<StoreInventory> findByStoreId(Long storeId);

    List<StoreInventory> findByStoreIdAndAvailableTrue(Long storeId);

    List<StoreInventory> findByProductId(Long productId);

    Optional<StoreInventory> findByStoreIdAndProductId(Long storeId, Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT si FROM StoreInventory si WHERE si.store.id = :storeId AND si.product.id = :productId")
    Optional<StoreInventory> findByStoreIdAndProductIdWithLock(@Param("storeId") Long storeId, @Param("productId") Long productId);

    @Query("SELECT si FROM StoreInventory si WHERE si.store.id = :storeId AND si.stockQuantity <= si.lowStockThreshold")
    List<StoreInventory> findLowStockInventories(@Param("storeId") Long storeId);

    @Query("SELECT si FROM StoreInventory si WHERE si.stockQuantity <= si.lowStockThreshold")
    List<StoreInventory> findAllLowStockInventories();
}
