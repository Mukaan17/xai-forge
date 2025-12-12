package com.example.xaiapp.repository;

import com.example.xaiapp.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Webhook entity operations.
 */
@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    /**
     * Find all webhooks for a user.
     */
    @Query("SELECT w FROM Webhook w WHERE w.user.id = :userId ORDER BY w.createdAt DESC")
    List<Webhook> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Find all active webhooks for a user.
     */
    @Query("SELECT w FROM Webhook w WHERE w.user.id = :userId AND w.active = true")
    List<Webhook> findByUserIdAndActiveTrue(@Param("userId") Long userId);

    /**
     * Find webhook by ID for a specific user.
     */
    @Query("SELECT w FROM Webhook w WHERE w.id = :id AND w.user.id = :userId")
    Optional<Webhook> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Find active webhooks subscribed to a specific event.
     * Uses native PostgreSQL JSONB query for array containment.
     */
    @Query(value = "SELECT * FROM webhooks WHERE active = true " +
           "AND events @> CAST(:event AS jsonb)", nativeQuery = true)
    List<Webhook> findActiveWebhooksForEvent(@Param("event") String event);

    /**
     * Find auto-disabled webhooks.
     */
    @Query("SELECT w FROM Webhook w WHERE w.autoDisabled = true")
    List<Webhook> findAutoDisabledWebhooks();

    /**
     * Count webhooks for a user.
     */
    @Query("SELECT COUNT(w) FROM Webhook w WHERE w.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
