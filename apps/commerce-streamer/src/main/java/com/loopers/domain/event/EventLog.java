package com.loopers.domain.event;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 이벤트 감사 로그 엔티티
 */
@Entity
@Table(name = "event_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class EventLog {

    @Id
    @Column(name = "event_id", length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @Column(name = "partition_key", nullable = false)
    private Integer partitionId;

    @Column(name = "offset_value", nullable = false)
    private Long offsetValue;

    @Column(name = "key_value", length = 200)
    private String keyValue;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "event_occurred_at", nullable = false)
    private LocalDateTime eventOccurredAt;

    @CreatedDate
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public static EventLog of(
            String eventId,
            String eventType,
            String topic,
            Integer partitionId,
            Long offsetValue,
            String keyValue,
            String payload,
            LocalDateTime eventOccurredAt
    ) {
        EventLog eventLog = new EventLog();
        eventLog.eventId = eventId;
        eventLog.eventType = eventType;
        eventLog.topic = topic;
        eventLog.partitionId = partitionId;
        eventLog.offsetValue = offsetValue;
        eventLog.keyValue = keyValue;
        eventLog.payload = payload;
        eventLog.eventOccurredAt = eventOccurredAt;
        return eventLog;
    }
}