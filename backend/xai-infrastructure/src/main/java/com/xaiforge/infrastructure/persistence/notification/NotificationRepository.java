package com.xaiforge.infrastructure.persistence.notification;

import com.xaiforge.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @org.springframework.data.jpa.repository.Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdOrderByCreatedAtDesc(@org.springframework.data.repository.query.Param("userId") Long userId, Pageable pageable);
    
    @org.springframework.data.jpa.repository.Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(@org.springframework.data.repository.query.Param("userId") Long userId);
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false")
    long countByUserIdAndReadFalse(@org.springframework.data.repository.query.Param("userId") Long userId);
}

