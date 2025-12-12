package com.example.xaiapp.repository;

import com.example.xaiapp.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ActivityLog entity operations.
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Find all activity logs for a user, ordered by creation date.
     */
    @Query("SELECT a FROM ActivityLog a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find activity logs for a user within a date range.
     */
    @Query("SELECT a FROM ActivityLog a WHERE a.user.id = :userId " +
           "AND a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByUserIdAndCreatedAtBetween(
        @Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );

    /**
     * Find activity logs by action type for a user.
     */
    @Query("SELECT a FROM ActivityLog a WHERE a.user.id = :userId AND a.action = :action ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByUserIdAndActionOrderByCreatedAtDesc(
        @Param("userId") Long userId,
        @Param("action") ActivityLog.ActionType action,
        Pageable pageable
    );

    /**
     * Find recent activity logs for a user.
     */
    @Query("SELECT a FROM ActivityLog a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Count activity logs by action type for a user.
     */
    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.user.id = :userId AND a.action = :action")
    long countByUserIdAndAction(@Param("userId") Long userId, @Param("action") ActivityLog.ActionType action);

    /**
     * Count activity logs by action type since a date.
     */
    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.user.id = :userId AND a.action = :action AND a.createdAt >= :since")
    long countByUserIdAndActionSince(
        @Param("userId") Long userId,
        @Param("action") ActivityLog.ActionType action,
        @Param("since") LocalDateTime since
    );

    /**
     * Delete old activity logs.
     */
    @Modifying
    @Query("DELETE FROM ActivityLog a WHERE a.user.id = :userId AND a.createdAt < :before")
    void deleteOldLogs(@Param("userId") Long userId, @Param("before") LocalDateTime before);

    /**
     * Find activity logs by multiple action types.
     */
    @Query("SELECT a FROM ActivityLog a WHERE a.user.id = :userId AND a.action IN :actions ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByUserIdAndActionIn(
        @Param("userId") Long userId,
        @Param("actions") List<ActivityLog.ActionType> actions,
        Pageable pageable
    );
}
