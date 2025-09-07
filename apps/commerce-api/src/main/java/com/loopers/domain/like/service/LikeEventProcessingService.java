package com.loopers.domain.like.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeSummaryRepository;
import com.loopers.domain.like.event.LikeChangedEvent;
import com.loopers.domain.like.event.LikeEvent;
import com.loopers.infrastructure.event.KafkaEventPublisher;
import com.loopers.infrastructure.kafka.PartitionKeyStrategy;
import com.loopers.message.KafkaEventMessage;
import com.loopers.message.KafkaTopics;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Like 이벤트 처리 서비스 - 실제 비즈니스 로직 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LikeEventProcessingService {

    private final LikeSummaryRepository likeSummaryRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final PartitionKeyStrategy partitionKeyStrategy;
    private final ObjectMapper objectMapper;

    /**
     * Like 이벤트 처리 (LikeSummary 업데이트 + Kafka 이벤트 발행)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processLikeEvent(LikeEvent likeEvent) throws JsonProcessingException {
        log.info("Processing LikeEvent - productId: {}, userId: {}, increment: {}",
                likeEvent.getProductId(), likeEvent.getUserId(), likeEvent.isIncrement());

        try {
            // 1. LikeSummary 업데이트
            LikeSummary updatedSummary = updateLikeSummary(likeEvent);

            // 2. Kafka 이벤트 발행
            publishLikeChangedEvent(likeEvent, updatedSummary.getLikesCount());

            log.info("LikeEvent processing completed - productId: {}, final count: {}",
                    likeEvent.getProductId(), updatedSummary.getLikesCount());

        } catch (Exception e) {
            log.error("LikeEvent processing failed - productId: {}, userId: {}, error: {}",
                    likeEvent.getProductId(), likeEvent.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * LikeSummary 업데이트
     */
    private LikeSummary updateLikeSummary(LikeEvent likeEvent) {
        LikeSummary likeSummary = likeSummaryRepository.likeSummaryByProductId(likeEvent.getProductId());

        if (likeEvent.isIncrement()) {
            likeSummary.increaseLikesCount();
            log.info("Like count increased - productId: {}, new count: {}",
                    likeEvent.getProductId(), likeSummary.getLikesCount());
        } else {
            likeSummary.decreaseLikesCount();
            log.info("Like count decreased - productId: {}, new count: {}",
                    likeEvent.getProductId(), likeSummary.getLikesCount());
        }

        likeSummaryRepository.updateLikeSummary(likeSummary);
        return likeSummary;
    }

    /**
     * LikeChanged 이벤트를 Kafka로 발행
     */
    private void publishLikeChangedEvent(LikeEvent likeEvent, Long currentLikesCount) throws JsonProcessingException {
        String action = likeEvent.isIncrement() ? "LIKED" : "UNLIKED";
        LikeChangedEvent likeChangedEvent = new LikeChangedEvent(
                likeEvent.getProductId(),
                likeEvent.getUserId(),
                action,
                currentLikesCount
        );

        KafkaEventMessage<Object> message = KafkaEventMessage.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(action)
            .aggregateId(likeEvent.getProductId())
            .timestamp(LocalDateTime.now())
            .version(1)
            .payload(objectMapper.writeValueAsString(likeChangedEvent))
            .build();

        // 파티션 키 생성 (상품별 순서 보장)
        String partitionKey = partitionKeyStrategy.getCatalogEventPartitionKey(likeEvent.getProductId());
        kafkaEventPublisher.publishEvent(KafkaTopics.CATALOG_EVENT, partitionKey, message);
    }
}
