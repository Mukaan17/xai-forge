/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:05:46
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:27
 */
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
