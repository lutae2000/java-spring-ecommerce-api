package com.loopers.application.event;

import java.time.LocalDateTime;

public interface Event {
    /**
     * 이벤트 발생 시간
     */
    LocalDateTime getOccurredAt();

    /**
     * 이벤트 타입
     */
     String getEventType();

    /**
     * Aggregate ID (Kafka Partition Key로 사용)
     */
    String getAggregateId();

    /**
     * 이벤트 버전 (순서 보장용)
     */
    default Long getVersion() {
        return getOccurredAt().toEpochSecond(java.time.ZoneOffset.UTC);
    }
}
