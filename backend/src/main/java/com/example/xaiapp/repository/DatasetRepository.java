package com.example.xaiapp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.User;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    
    List<Dataset> findByOwner(User owner);
    
    List<Dataset> findByOwnerId(Long ownerId);
    
    Optional<Dataset> findByIdAndOwner(Long id, User owner);
    
    Optional<Dataset> findByIdAndOwnerId(Long id, Long ownerId);
}
