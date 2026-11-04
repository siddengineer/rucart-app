package com.rucart.backend.repository;

import com.rucart.backend.model.RiderCommissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderCommissionConfigRepository extends JpaRepository<RiderCommissionConfig, Long> {
    Optional<RiderCommissionConfig> findFirstByActiveTrueOrderByUpdatedAtDesc();
}
