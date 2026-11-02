package com.rucart.backend.repository;

import com.rucart.backend.model.StockAdjustmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAdjustmentHistoryRepository extends JpaRepository<StockAdjustmentHistory, Long> {

    List<StockAdjustmentHistory> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    List<StockAdjustmentHistory> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<StockAdjustmentHistory> findByStoreIdAndProductIdOrderByCreatedAtDesc(Long storeId, Long productId);
}
