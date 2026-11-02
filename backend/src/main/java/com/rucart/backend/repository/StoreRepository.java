package com.rucart.backend.repository;

import com.rucart.backend.model.Store;
import com.rucart.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByOpenTrue();
    List<Store> findByActiveTrue();
    List<Store> findByActiveTrueAndOpenTrue();
    List<Store> findByApprovedTrueAndActiveTrueAndOpenTrue();
    List<Store> findByApprovedTrueAndActiveTrue();
    Optional<Store> findByOwner(User owner);
    Optional<Store> findByOwnerId(Long ownerId);
    List<Store> findByCityIgnoreCase(String city);
    List<Store> findAllByOrderByCreatedAtDesc();
    List<Store> findByNameContainingIgnoreCase(String name);
    long countByActiveTrue();
}
