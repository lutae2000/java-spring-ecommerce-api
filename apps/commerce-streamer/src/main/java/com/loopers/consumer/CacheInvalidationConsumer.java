package com.loopers.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.message.KafkaEventMessage;
import com.loopers.message.KafkaTopics;
import com.loopers.service.cache.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 캐시 무효화 전용 컨슈머
 * StockOut, LikeChanged 이벤트 수신 시 관련 캐시 삭제
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheInvalidationConsumer {

    private final ProductCacheService productCacheService;
    private final ObjectMapper objectMapper;

    /**
     * 카탈로그 이벤트 수신하여 캐시 무효화 처리
     */
    @KafkaListener(
        topics = {KafkaTopics.CATALOG_EVENT},
        groupId = "cache-invalidation-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCacheInvalidationEvent(
            String jsonMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "kafka_receivedPartitionId", required = false) Object partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("[CACHE] 캐시 무효화 이벤트 처리 시작 - Topic: {}, Partition: {}, Offset: {}", 
                topic, partition, offset);

            if (jsonMessage == null || jsonMessage.trim().isEmpty()) {
                log.warn("[CACHE] 빈 캐시 무효화 메시지 - Skip");
                acknowledgment.acknowledge();
                return;
            }

            // JSON 파싱하여 메타데이터 추출
            @SuppressWarnings("unchecked")
            KafkaEventMessage<String> kafkaEventMessage = objectMapper.readValue(jsonMessage, KafkaEventMessage.class);
            
            String eventType = kafkaEventMessage.getEventType();
            String productId = kafkaEventMessage.getAggregateId();

            log.debug("[CACHE] 캐시 무효화 대상 - EventType: {}, ProductId: {}", eventType, productId);

            // 이벤트 타입에 따른 캐시 무효화 처리
            processCacheInvalidation(eventType, productId);

            // 성공 ACK
            acknowledgment.acknowledge();
            log.debug("[CACHE] 캐시 무효화 처리 완료 - ProductId: {}, EventType: {}", productId, eventType);

        } catch (Exception e) {
            log.error("[CACHE] 캐시 무효화 처리 실패 - Topic: {}, Offset: {}, Error: {}", 
                topic, offset, e.getMessage(), e);
            
            // 실패 시에도 ACK (캐시 삭제 실패가 전체 시스템을 멈추지 않도록)
            acknowledgment.acknowledge();
        }
    }

    /**
     * 이벤트 타입에 따른 캐시 무효화 처리
     */
    private void processCacheInvalidation(String eventType, String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            log.warn("[CACHE] ProductId가 없어서 캐시 무효화를 건너뜁니다 - EventType: {}", eventType);
            return;
        }

        productCacheService.deleteProductCache(productId);

    }
}
