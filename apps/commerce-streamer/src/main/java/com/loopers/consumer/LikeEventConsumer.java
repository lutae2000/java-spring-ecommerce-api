package com.loopers.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.like.event.LikeChangedEvent;
import com.loopers.message.KafkaEventMessage;
import com.loopers.message.KafkaTopics;
import com.loopers.service.metrics.ProductMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 범용 이벤트 Consumer - KafkaEventMessage 구조 처리
 * Producer에서 KafkaEventMessage 래퍼로 감싼 메시지를 올바르게 처리
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LikeEventConsumer {

    private final ObjectMapper objectMapper;
    private final ProductMetricsService productMetricsService;

    @KafkaListener(
        topics = KafkaTopics.CATALOG_EVENT,
        groupId = "generic-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCatalogEvent(
            String jsonMessage,  // String으로 받아서 직접 파싱
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "kafka_receivedPartitionId", required = false) Object partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("=== Catalog Event 수신 ===");
        log.info("Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);
        
        try {
            // 1. 기본 검증
            if (jsonMessage == null || jsonMessage.trim().isEmpty()) {
                log.warn("빈 메시지 수신 - Skip");
                acknowledgment.acknowledge();
                return;
            }
            
            log.info("Raw JSON Message: {}", jsonMessage);
            
            // 2. JSON을 KafkaEventMessage로 파싱
            KafkaEventMessage<String> kafkaEventMessage = objectMapper.readValue(jsonMessage, KafkaEventMessage.class);
            
            // 3. 메타데이터 추출
            String eventId = kafkaEventMessage.getEventId();
            String eventType = kafkaEventMessage.getEventType();
            String aggregateId = kafkaEventMessage.getAggregateId();
            LocalDateTime timestamp = kafkaEventMessage.getTimestamp();
            Integer version = kafkaEventMessage.getVersion();
            String payload = kafkaEventMessage.getPayload();
            
            log.info("📋 KafkaEventMessage 메타데이터:");
            log.info("  - eventId: {}", eventId);
            log.info("  - eventType: {}", eventType);
            log.info("  - aggregateId: {}", aggregateId);
            log.info("  - timestamp: {}", timestamp);
            log.info("  - version: {}", version);
            
            // 4. Payload 파싱 및 비즈니스 로직 처리 (Audit Log는 별도 Consumer에서 처리)
            if (payload != null && !payload.trim().isEmpty()) {
                processEventPayload(eventType, aggregateId, payload, eventId);
            } else {
                log.warn("⚠️ Payload가 비어있음");
            }
            
            // 5. 성공 ACK
            acknowledgment.acknowledge();
            log.info("✅ 메시지 처리 완료 - ACK");
            
        } catch (Exception e) {
            log.error("❌ Catalog Event 처리 실패", e);
            log.error("Raw JSON Message: {}", jsonMessage);
            // 실패 시 ACK하지 않음 (재처리 대상)
        }
    }
    
    /**
     * 이벤트 타입별 Payload 처리
     */
    private void processEventPayload(String eventType, String aggregateId, String payload, String eventId) throws JsonProcessingException {
        try {
            log.info("🔄 Payload 처리 시작 - EventType: {}", eventType);
            
            switch (eventType) {
                case "LIKED", "UNLIKED" -> processLikeEvent(eventType, aggregateId, payload, eventId);
                default -> {
                    log.info("📦 알 수 없는 이벤트 타입 - 범용 처리: {}", eventType);
                    Object genericPayload = objectMapper.readValue(payload, Object.class);
                    log.info("Generic payload: {}", genericPayload);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Payload 처리 실패 - EventType: {}, Error: {}", eventType, e.getMessage(), e);
            throw new RuntimeException("Payload processing failed", e);
        }
    }
    
    /**
     * Like 이벤트 처리 (LIKED/UNLIKED)
     */
    private void processLikeEvent(String eventType, String aggregateId, String payload, String eventId) throws JsonProcessingException {
        try {
            // Payload를 LikeChangedEvent로 파싱
            LikeChangedEvent likeEvent = objectMapper.readValue(payload, LikeChangedEvent.class);
            
            log.info("✅ LikeChangedEvent 파싱 성공:");
            log.info("  - ProductId: {}", likeEvent.getProductId());
            log.info("  - UserId: {}", likeEvent.getUserId());
            log.info("  - Action: {}", likeEvent.getAction());
            log.info("  - CurrentLikesCount: {}", likeEvent.getCurrentLikesCount());
            log.info("  - OccurredAt: {}", likeEvent.getOccurredAt());
            
            // Metrics 업데이트 (일별 좋아요 수 집계)
            productMetricsService.updateLikesCount(
                likeEvent.getProductId(), 
                likeEvent.getCurrentLikesCount(),
                LocalDate.now()
            );
            
            log.info("✅ Like Event 처리 완료 - ProductId: {}, Action: {}", 
                likeEvent.getProductId(), likeEvent.getAction());
                
        } catch (Exception e) {
            log.error("❌ Like Event 처리 실패", e);
            throw e;
        }
    }
}
