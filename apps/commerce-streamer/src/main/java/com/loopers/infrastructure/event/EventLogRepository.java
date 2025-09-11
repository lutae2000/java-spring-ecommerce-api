package com.loopers.infrastructure.event;

import com.loopers.domain.event.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 이벤트 로그 Repository
 */
@Repository
public interface EventLogRepository extends JpaRepository<EventLog, String> {
    
    /**
     * 이벤트 ID로 조회 (중복 처리 확인용)
     */
    boolean existsByEventId(String eventId);
}