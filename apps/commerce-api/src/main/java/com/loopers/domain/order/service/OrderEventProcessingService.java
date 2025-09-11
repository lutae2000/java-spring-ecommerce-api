package com.loopers.domain.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.order.event.OrderCreatedKafkaEvent;
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
 * Order 이벤트 처리 서비스 - 실제 비즈니스 로직 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProcessingService {

    private final KafkaEventPublisher kafkaEventPublisher;
    private final PartitionKeyStrategy partitionKeyStrategy;
    private final ObjectMapper objectMapper;
    /**
     * OrderCreated 이벤트 처리 (Kafka 이벤트 발행)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent) throws JsonProcessingException {
        log.info("Processing OrderCreatedEvent - orderId: {}, userId: {}",
                orderCreatedEvent.getOrder().getId(), orderCreatedEvent.getUserId());

        try {
            // 1. Kafka 이벤트 발행
            publishOrderCreatedEvent(orderCreatedEvent);

            log.info("OrderCreatedEvent processing completed - orderId: {}",
                    orderCreatedEvent.getOrder().getId());

        } catch (Exception e) {
            log.error("OrderCreatedEvent processing failed - orderId: {}, userId: {}, error: {}",
                    orderCreatedEvent.getOrder().getId(), orderCreatedEvent.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * OrderCreated 이벤트를 Kafka로 발행
     */
    private void publishOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent) throws JsonProcessingException {
        log.info("Publishing OrderCreatedEvent to Kafka - orderId: {}", orderCreatedEvent.getOrder().getId());

        // 순환 참조 방지를 위해 별도 DTO로 변환
        OrderCreatedKafkaEvent kafkaEvent = OrderCreatedKafkaEvent.from(orderCreatedEvent);

        KafkaEventMessage<Object> message = KafkaEventMessage.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(String.valueOf(OrderStatus.ORDER_PLACED))
            .aggregateId(orderCreatedEvent.getOrder().getOrderNo())
            .timestamp(LocalDateTime.now())
            .version(1)
            .payload(objectMapper.writeValueAsString(kafkaEvent))  // DTO 사용으로 순환 참조 방지
            .build();

        // 파티션 키 생성 (주문별 순서 보장)
        String partitionKey = partitionKeyStrategy.getOrderEventPartitionKey(orderCreatedEvent.getOrder().getOrderNo());
        kafkaEventPublisher.publishEvent(KafkaTopics.ORDER_EVENT, partitionKey, message);
        
        log.info("OrderCreatedEvent published successfully - orderNo: {}", orderCreatedEvent.getOrder().getOrderNo());
    }
}
