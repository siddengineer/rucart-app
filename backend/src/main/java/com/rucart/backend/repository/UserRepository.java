package com.rucart.backend.repository;

import com.rucart.backend.model.Role;
import com.rucart.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneOrEmail(String phone, String email);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    long countByRole(Role role);
}
