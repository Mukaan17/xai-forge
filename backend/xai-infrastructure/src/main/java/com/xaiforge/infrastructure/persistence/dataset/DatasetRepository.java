package com.xaiforge.infrastructure.persistence.dataset;

import com.xaiforge.domain.dataset.entity.Dataset;
import com.xaiforge.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    
    List<Dataset> findByOwner(User owner);
    
    List<Dataset> findByOwnerId(Long ownerId);
    
    Optional<Dataset> findByIdAndOwner(Long id, User owner);
    
    Optional<Dataset> findByIdAndOwnerId(Long id, Long ownerId);
    
    @Query("SELECT COUNT(d) FROM Dataset d WHERE d.owner.id = :ownerId")
    long countByOwnerId(Long ownerId);
    
    // Search and filter methods
    @Query("SELECT d FROM Dataset d WHERE d.owner.id = :ownerId " +
           "AND (:search IS NULL OR LOWER(d.fileName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY d.uploadDate DESC")
    Page<Dataset> findByOwnerIdWithSearch(@Param("ownerId") Long ownerId, 
                                          @Param("search") String search, 
                                          Pageable pageable);
    
    @Query("SELECT d FROM Dataset d WHERE d.owner.id = :ownerId " +
           "AND (:search IS NULL OR LOWER(d.fileName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:dateFrom IS NULL OR d.uploadDate >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.uploadDate <= :dateTo) " +
           "ORDER BY d.uploadDate DESC")
    Page<Dataset> findByOwnerIdWithFilters(@Param("ownerId") Long ownerId,
                                           @Param("search") String search,
                                           @Param("dateFrom") LocalDateTime dateFrom,
                                           @Param("dateTo") LocalDateTime dateTo,
                                           Pageable pageable);
}

