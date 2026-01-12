package com.babyshop.api.user.repository;

import com.babyshop.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email (case-insensitive)
     */
    Optional<User> findByEmailIgnoreCaseAndIsDeletedFalse(String email);

    /**
     * Find active user by email
     */
    Optional<User> findByEmailIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmailIgnoreCaseAndIsDeletedFalse(String email);

    /**
     * Find by phone number
     */
    Optional<User> findByPhoneNumberAndIsDeletedFalse(String phoneNumber);
}

