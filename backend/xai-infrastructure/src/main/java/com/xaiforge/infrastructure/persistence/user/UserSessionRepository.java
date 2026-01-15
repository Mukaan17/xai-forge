package com.xaiforge.infrastructure.persistence.user;

import com.xaiforge.domain.user.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    List<UserSession> findByUserIdAndIsActiveTrue(Long userId);
    
    Optional<UserSession> findBySessionToken(String sessionToken);
    
    Optional<UserSession> findByIdAndUserId(Long id, Long userId);
    
    void deleteByUserIdAndIsActiveFalse(Long userId);
}
