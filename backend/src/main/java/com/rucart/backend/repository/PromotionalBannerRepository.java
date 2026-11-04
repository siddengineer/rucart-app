package com.rucart.backend.repository;

import com.rucart.backend.model.PromotionalBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionalBannerRepository extends JpaRepository<PromotionalBanner, Long> {
    List<PromotionalBanner> findByActiveTrueOrderByDisplayOrderAsc();
    List<PromotionalBanner> findAllByOrderByDisplayOrderAsc();
}
