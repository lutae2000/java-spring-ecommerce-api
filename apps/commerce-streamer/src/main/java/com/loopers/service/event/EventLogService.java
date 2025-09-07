package com.loopers.service.event;

import com.loopers.domain.event.EventLog;
import com.loopers.infrastructure.event.EventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 이벤트 로그 저장 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventLogService {

    private final EventLogRepository eventLogRepository;

    /**
     * 이벤트 로그 저장 (멱등성 보장)
     */
    @Transactional
    public void saveEventLog(
            String eventId,
            String eventType,
            String topic,
            Integer partitionId,
            Long offsetValue,
            String keyValue,
            String eventPayload,
            LocalDateTime eventOccurredAt
    ) {
        // 디버깅을 위한 로그 추가
        log.debug("EventLog 저장 파라미터: eventId={}, eventType={}, topic={}, partitionId={}, offsetValue={}, keyValue={}, eventOccurredAt={}", 
                eventId, eventType, topic, partitionId, offsetValue, keyValue, eventOccurredAt);
        
        // 필수 값 검증 및 기본값 설정
        if (partitionId == null) {
            partitionId = 0;
            log.warn("partitionId가 null입니다. 기본값 0으로 설정합니다. - eventId: {}", eventId);
        }
        if (offsetValue == null) {
            offsetValue = 0L;
            log.warn("offsetValue가 null입니다. 기본값 0으로 설정합니다. - eventId: {}", eventId);
        }
        if (eventOccurredAt == null) {
            eventOccurredAt = LocalDateTime.now();
            log.warn("eventOccurredAt가 null입니다. 현재 시간으로 설정합니다. - eventId: {}", eventId);
        }
        
        // 중복 처리 방지 (멱등성 보장)
        if (eventLogRepository.existsByEventId(eventId)) {
            log.info("이벤트 로그 이미 존재 - eventId: {}", eventId);
            return;
        }

        try {
            // 이벤트 로그 생성 및 저장
            EventLog eventLog = EventLog.of(
                    eventId,
                    eventType,
                    topic,
                    partitionId,
                    offsetValue,
                    keyValue,
                    eventPayload,
                    eventOccurredAt
            );

            eventLogRepository.save(eventLog);
            
            log.info("이벤트 로그 저장 완료 - eventId: {}, eventType: {}, topic: {}", 
                    eventId, eventType, topic);

        } catch (Exception e) {
            log.error("이벤트 로그 저장 실패 - eventId: {}, error: {}", 
                    eventId, e.getMessage(), e);
            throw e;
        }
    }
}
