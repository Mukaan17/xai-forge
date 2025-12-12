/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:05:46
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:27
 */
package com.example.xaiapp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.User;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    
    // Backward compatibility methods (using explicit queries since entity uses 'user' not 'owner')
    @Query("SELECT d FROM Dataset d WHERE d.user = :owner")
    List<Dataset> findByOwner(@Param("owner") User owner);
    
    @Query("SELECT d FROM Dataset d WHERE d.user.id = :ownerId")
    List<Dataset> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT d FROM Dataset d WHERE d.id = :id AND d.user = :owner")
    Optional<Dataset> findByIdAndOwner(@Param("id") Long id, @Param("owner") User owner);
    
    @Query("SELECT d FROM Dataset d WHERE d.id = :id AND d.user.id = :ownerId")
    Optional<Dataset> findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);
    
    // New methods for enhanced Dataset entity
    @Query("SELECT d FROM Dataset d WHERE d.user.id = :userId AND d.deleted = false ORDER BY d.createdAt DESC")
    List<Dataset> findByUserIdAndDeletedFalse(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(d) FROM Dataset d WHERE d.user.id = :userId AND d.deleted = false")
    long countByUserIdAndDeletedFalse(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(d) FROM Dataset d WHERE d.user.id = :userId AND d.createdAt >= :after")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("after") LocalDateTime after);
    
    @Query("SELECT COALESCE(SUM(d.fileSizeBytes), 0) FROM Dataset d WHERE d.user.id = :userId AND d.deleted = false")
    Long getTotalFileSizeByUserId(@Param("userId") Long userId);
}
