package com.example.xaiapp.repository;

import com.example.xaiapp.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserPreferences entity operations.
 */
@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    /**
     * Find preferences by user ID.
     */
    @Query("SELECT p FROM UserPreferences p WHERE p.user.id = :userId")
    Optional<UserPreferences> findByUserId(@Param("userId") Long userId);

    /**
     * Check if preferences exist for a user.
     */
    @Query("SELECT COUNT(p) > 0 FROM UserPreferences p WHERE p.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
