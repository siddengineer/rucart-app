package com.rucart.backend.repository;

import com.rucart.backend.model.RiderCommissionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiderCommissionHistoryRepository extends JpaRepository<RiderCommissionHistory, Long> {
    List<RiderCommissionHistory> findAllByOrderByChangedAtDesc();
}
