package com.xaiforge.infrastructure.persistence.activity;

import com.xaiforge.domain.activity.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    Page<ActivityLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    List<ActivityLog> findByUserIdAndTimestampAfterOrderByTimestampDesc(Long userId, LocalDateTime after, Pageable pageable);
}

