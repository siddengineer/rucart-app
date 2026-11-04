package com.rucart.backend.repository;

import com.rucart.backend.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    Optional<Offer> findByCodeIgnoreCaseAndActiveTrue(String code);
    List<Offer> findByActiveTrueAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(LocalDate date);
    List<Offer> findAllByOrderByCreatedAtDesc();
}
