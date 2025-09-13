package com.loopers.domain.rank.event;


import com.loopers.infrastructure.event.KafkaEventPublisher;
import com.loopers.infrastructure.kafka.PartitionKeyStrategy;
import com.loopers.message.KafkaEventMessage;
import com.loopers.message.KafkaTopics;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingEventPublisher {
    private final KafkaEventPublisher kafkaEventPublisher;
    private final PartitionKeyStrategy partitionKeyStrategy;

    /**
     * 상품 조회 이벤트 발행
     */
    public void publishViewEvent(String productId) {
        RankingEvent event = RankingEvent.createViewEvent(productId);
        publishRankingEvent(event);
    }

    /**
     * 좋아요 이벤트 발행
     */
    public void publishLikeEvent(String productId, String userId) {
        RankingEvent event = RankingEvent.createLikeEvent(productId);
        publishRankingEvent(event);
    }

    /**
     * 주문 이벤트 발행
     */
    public void publishOrderEvent(String productId, String userId, Double orderAmount) {
        RankingEvent event = RankingEvent.createOrderEvent(productId, userId, orderAmount);
        publishRankingEvent(event);
    }

    /**
     * 랭킹 이벤트 발행
     */
    private void publishRankingEvent(RankingEvent event) {
        try {
            String key = partitionKeyStrategy.generateKey(event.getProductId());
            KafkaEventMessage<RankingEvent> message = KafkaEventMessage.<RankingEvent>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(event.getEventType())
                .aggregateId(event.getProductId())
                .timestamp(LocalDateTime.now())
                .version(1)
                .payload(event)
                .build();

            kafkaEventPublisher.publishEvent(
                KafkaTopics.RANKING_EVENTS,
                key,
                message
            );

            log.debug("랭킹 이벤트 발행 완료 - productId: {}, eventType: {}",
                event.getProductId(), event.getEventType());

        } catch (Exception e) {
            log.error("랭킹 이벤트 발행 실패 - event: {}", event, e);
            throw new RuntimeException("랭킹 이벤트 발행 실패", e);
        }
    }
}
