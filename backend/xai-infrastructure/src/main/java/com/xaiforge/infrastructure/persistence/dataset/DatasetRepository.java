package com.xaiforge.infrastructure.persistence.dataset;

import com.xaiforge.domain.dataset.entity.Dataset;
import com.xaiforge.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}

