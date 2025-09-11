package com.loopers.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.message.KafkaEventMessage;
import com.loopers.message.KafkaTopics;
import com.loopers.service.event.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 감사 로그 전용 Consumer
 * 모든 Kafka 이벤트를 수신하여 감사 로그(Audit Log)로 저장
 * 
 * 특징:
 * - 모든 토픽 구독 (catalog-events, order-events)
 * - 순수 감사 목적으로만 사용 (비즈니스 로직 없음)
 * - 높은 처리량과 안정성 보장
 * - 별도 Consumer Group으로 독립 운영
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogConsumer {

    private final EventLogService eventLogService;
    private final ObjectMapper objectMapper;

    /**
     * 모든 이벤트 감사 로그 저장
     */
    @KafkaListener(
        topics = {KafkaTopics.CATALOG_EVENT, KafkaTopics.ORDER_EVENT},
        groupId = "audit-log-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void auditAllEvents(
            String jsonMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "kafka_receivedPartitionId", required = false) Object partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        // topic에 따라 도메인 결정
        String domain = topic.equals(KafkaTopics.CATALOG_EVENT) ? "CATALOG" : "ORDER";
        processAuditLog(domain, jsonMessage, topic, partition, offset, acknowledgment);
    }


    /**
     * 공통 감사 로그 처리 로직
     */
    private void processAuditLog(
            String domain,
            String jsonMessage,
            String topic,
            Object partition,
            long offset,
            Acknowledgment acknowledgment) {
        
        try {
            if (jsonMessage == null || jsonMessage.trim().isEmpty()) {
                log.warn("[{}] 빈 감사 로그 메시지 - Skip", domain);
                acknowledgment.acknowledge();
                return;
            }

            // JSON 메시지 길이 및 중첩 깊이 검증
            if (jsonMessage.length() > 100000) {  // 100KB 제한
                log.error("[{}] JSON 메시지가 너무 큽니다: {} bytes", domain, jsonMessage.length());
                acknowledgment.acknowledge();
                return;
            }
            
            // 중첩 깊이 간단 검증 (대괄호/중괄호 카운트)
            long openBraces = jsonMessage.chars().filter(ch -> ch == '{' || ch == '[').count();
            if (openBraces > 500) {  // 500개 제한
                log.error("[{}] JSON 중첩이 너무 깊습니다: {} 개의 열린 브레이스", domain, openBraces);
                acknowledgment.acknowledge();
                return;
            }

            // JSON 파싱하여 메타데이터 추출
            KafkaEventMessage<String> kafkaEventMessage = objectMapper.readValue(jsonMessage, KafkaEventMessage.class);
            
            String eventId = kafkaEventMessage.getEventId();
            String eventType = kafkaEventMessage.getEventType();
            String aggregateId = kafkaEventMessage.getAggregateId();
            LocalDateTime timestamp = kafkaEventMessage.getTimestamp();

            log.debug("[{}] 감사 로그 저장 - EventId: {}, EventType: {}, AggregateId: {}", 
                domain, eventId, eventType, aggregateId);

            // 감사 로그 저장 (멱등성 보장)
            Integer partitionId = null;
            if (partition instanceof Integer) {
                partitionId = (Integer) partition;
            } else if (partition instanceof String) {
                try {
                    partitionId = Integer.parseInt((String) partition);
                } catch (NumberFormatException e) {
                    log.warn("[{}] partition 값을 Integer로 변환할 수 없습니다: {}", domain, partition);
                    partitionId = 0;
                }
            } else {
                log.warn("[{}] 알 수 없는 partition 타입: {}", domain, partition != null ? partition.getClass() : "null");
                partitionId = 0;
            }
            
            eventLogService.saveEventLog(
                eventId,
                eventType,
                topic,
                partitionId,
                offset,
                aggregateId,
                jsonMessage,  // 원본 JSON 문자열 저장
                timestamp != null ? timestamp : LocalDateTime.now()
            );

            // 성공 ACK
            acknowledgment.acknowledge();
            log.debug("[{}] 감사 로그 저장 완료 - EventId: {}", domain, eventId);

        } catch (Exception e) {
            log.error("[{}] 감사 로그 저장 실패 - JSON: {}, Error: {}", 
                domain, 
                jsonMessage, 
                e.getMessage(), e);
            
            // 감사 로그는 매우 중요하므로 실패 시 재처리
            // ACK하지 않아서 자동 재시도
        }
    }
}
