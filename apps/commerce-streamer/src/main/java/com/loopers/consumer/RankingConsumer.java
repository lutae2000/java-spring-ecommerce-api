package com.loopers.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.rank.RankingEvent;
import com.loopers.domain.rank.RankingService;
import com.loopers.message.KafkaEventMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingConsumer {

    private final RankingService rankingEventProcessingService;
    private final ObjectMapper objectMapper;
    /**
     * 랭킹 이벤트 배치 처리
     * - 조회, 좋아요, 주문 이벤트를 배치로 처리하여 Redis ZSET에 점수 누적
     */
    @KafkaListener(
        topics = "ranking-events",
        groupId = "ranking-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleRankingEvents(
        String messages,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("랭킹 이벤트 배치 처리 시작 - topic: {}, partition: {}, offset: {}",
            topic, partition, offset);

        try {
            // 1. 기본 검증
            if (messages == null || messages.trim().isEmpty()) {
                log.warn("빈 메시지 수신 - Skip");
                acknowledgment.acknowledge();
                return;
            }
            
            log.info("Raw JSON Message: {}", messages);
            
            // 2. JSON을 KafkaEventMessage로 파싱
            KafkaEventMessage<String> kafkaEventMessage = objectMapper.readValue(messages, KafkaEventMessage.class);
            
            // 3. 메타데이터 추출
            String eventId = kafkaEventMessage.getEventId();
            String eventType = kafkaEventMessage.getEventType();
            String aggregateId = kafkaEventMessage.getAggregateId();
            String payload = kafkaEventMessage.getPayload();
            
            log.info("📋 KafkaEventMessage 메타데이터:");
            log.info("  - eventId: {}", eventId);
            log.info("  - eventType: {}", eventType);
            log.info("  - aggregateId: {}", aggregateId);
            
            // 4. Payload에서 RankingEvent 파싱
            if (payload != null && !payload.trim().isEmpty()) {
                RankingEvent event = objectMapper.readValue(payload, RankingEvent.class);
                rankingEventProcessingService.processRankingEvent(event);
                
                log.info("✅ 랭킹 이벤트 처리 완료 - productId: {}", event.getProductId());
            } else {
                log.warn("⚠️ Payload가 비어있음");
            }

            // 5. 수동 커밋
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("랭킹 이벤트 처리 실패 - topic: {}, partition: {}, offset: {}, message: {}",
                topic, partition, offset, messages, e);
            // 에러 발생 시에도 커밋 (재처리 방지)
            acknowledgment.acknowledge();
        }
    }

}
