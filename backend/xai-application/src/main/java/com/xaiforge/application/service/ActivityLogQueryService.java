package com.xaiforge.application.service;

import com.xaiforge.domain.activity.entity.ActivityLog;
import com.xaiforge.infrastructure.persistence.activity.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityLogQueryService {
    
    private final ActivityLogRepository activityLogRepository;
    
    public Page<ActivityLog> searchActivityLogs(
            Long userId,
            String eventType,
            String search,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String ipAddress,
            Pageable pageable) {
        
        Specification<ActivityLog> spec = Specification.where(byUserId(userId));
        
        if (eventType != null && !eventType.isEmpty()) {
            try {
                ActivityLog.EventType type = ActivityLog.EventType.valueOf(eventType.toUpperCase());
                spec = spec.and(byEventType(type));
            } catch (IllegalArgumentException e) {
                // Invalid event type, ignore
            }
        }
        
        if (search != null && !search.isEmpty()) {
            spec = spec.and(bySearchTerm(search));
        }
        
        if (dateFrom != null) {
            spec = spec.and(afterDate(dateFrom));
        }
        
        if (dateTo != null) {
            spec = spec.and(beforeDate(dateTo));
        }
        
        if (ipAddress != null && !ipAddress.isEmpty()) {
            spec = spec.and(byIpAddress(ipAddress));
        }
        
        return activityLogRepository.findAll(spec, pageable);
    }
    
    private Specification<ActivityLog> byUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }
    
    private Specification<ActivityLog> byEventType(ActivityLog.EventType eventType) {
        return (root, query, cb) -> cb.equal(root.get("eventType"), eventType);
    }
    
    private Specification<ActivityLog> bySearchTerm(String search) {
        return (root, query, cb) -> {
            String searchPattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("details").as(String.class)), searchPattern),
                cb.like(cb.lower(root.get("ipAddress").as(String.class)), searchPattern),
                cb.like(cb.lower(root.get("userAgent").as(String.class)), searchPattern)
            );
        };
    }
    
    private Specification<ActivityLog> afterDate(LocalDateTime date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), date);
    }
    
    private Specification<ActivityLog> beforeDate(LocalDateTime date) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), date);
    }
    
    private Specification<ActivityLog> byIpAddress(String ipAddress) {
        return (root, query, cb) -> cb.like(root.get("ipAddress").as(String.class), "%" + ipAddress + "%");
    }
}
