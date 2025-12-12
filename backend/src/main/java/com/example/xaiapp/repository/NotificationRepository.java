package com.example.xaiapp.repository;

import com.example.xaiapp.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Notification entity operations.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a user, ordered by creation date.
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find unread notifications for a user.
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Count unread notifications for a user.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    long countByUserIdAndIsReadFalse(@Param("userId") Long userId);

    /**
     * Find notification by ID for a specific user.
     */
    @Query("SELECT n FROM Notification n WHERE n.id = :id AND n.user.id = :userId")
    Optional<Notification> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Mark all notifications as read for a user.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Delete old notifications.
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId AND n.createdAt < :before")
    void deleteOldNotifications(@Param("userId") Long userId, @Param("before") LocalDateTime before);

    /**
     * Find notifications by type for a user.
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.type = :type ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(
        @Param("userId") Long userId,
        @Param("type") Notification.NotificationType type,
        Pageable pageable
    );
}
