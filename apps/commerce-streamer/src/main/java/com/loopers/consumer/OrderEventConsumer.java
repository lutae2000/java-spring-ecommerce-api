package com.loopers.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.order.event.OrderCreatedKafkaEvent;
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
 * 주문 이벤트 Consumer
 * 주문/결제 관련 이벤트를 처리하여 비즈니스 로직 수행
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final ProductMetricsService productMetricsService;

    @KafkaListener(
        topics = KafkaTopics.ORDER_EVENT,
        groupId = "order-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderEvent(
            String jsonMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header("kafka_receivedPartitionId") int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("=== Order Event 수신 ===");
        log.info("Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);
        
        try {
            // 1. 기본 검증
            if (jsonMessage == null) {
                log.warn("빈 메시지 수신 - Skip");
                acknowledgment.acknowledge();
                return;
            }
            
            log.info("KafkaEventMessage: {}", jsonMessage);
            // 2. JSON을 KafkaEventMessage로 파싱
            KafkaEventMessage<String> kafkaEventMessage = objectMapper.readValue(jsonMessage, KafkaEventMessage.class);

            // 2. 메타데이터 추출
            String eventId = kafkaEventMessage.getEventId();
            String eventType = kafkaEventMessage.getEventType();
            String aggregateId = kafkaEventMessage.getAggregateId();
            LocalDateTime timestamp = kafkaEventMessage.getTimestamp();
            Integer version = kafkaEventMessage.getVersion();
            String payload = kafkaEventMessage.getPayload();
            
            log.info("📋 Order Event 메타데이터:");
            log.info("  - eventId: {}", eventId);
            log.info("  - eventType: {}", eventType);
            log.info("  - aggregateId: {}", aggregateId);
            log.info("  - timestamp: {}", timestamp);
            log.info("  - version: {}", version);
            
            // 3. Payload 파싱 및 비즈니스 로직 처리 (Audit Log는 별도 Consumer에서 처리)
            if (payload != null && !payload.trim().isEmpty()) {
                processOrderEventPayload(eventType, aggregateId, payload, eventId);
            } else {
                log.warn("⚠️ Payload가 비어있음");
            }
            
            // 4. 성공 ACK
            acknowledgment.acknowledge();
            log.info("✅ Order Event 처리 완료 - ACK");
            
        } catch (Exception e) {
            log.error("❌ Order Event 처리 실패", e);
            // 실패 시 ACK하지 않음 (재처리 대상)
        }
    }
    
    /**
     * 주문 이벤트 타입별 Payload 처리
     */
    private void processOrderEventPayload(String eventType, String aggregateId, String payload, String eventId) throws JsonProcessingException {
        try {
            log.info("🔄 Order Event Payload 처리 시작 - EventType: {}", eventType);
            
            switch (eventType) {
                case "ORDER_PLACED" -> processOrderCreatedEvent(eventType, aggregateId, payload, eventId);
                case "ORDER_PAID" -> processOrderPaidEvent(eventType, aggregateId, payload, eventId);
                case "ORDER_CANCELLED" -> processOrderCancelledEvent(eventType, aggregateId, payload, eventId);
                default -> {
                    log.info("📦 알 수 없는 주문 이벤트 타입 - 범용 처리: {}", eventType);
                    Object genericPayload = objectMapper.readValue(payload, Object.class);
                    log.info("Generic order payload: {}", genericPayload);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Order Event Payload 처리 실패 - EventType: {}, Error: {}", eventType, e.getMessage(), e);
            throw new RuntimeException("Order event payload processing failed", e);
        }
    }
    
    /**
     * 주문 생성 이벤트 처리
     */
    private void processOrderCreatedEvent(String eventType, String aggregateId, String payload, String eventId) throws JsonProcessingException {
        try {
            // Payload를 OrderCreatedKafkaEvent로 파싱
            OrderCreatedKafkaEvent orderEvent = objectMapper.readValue(payload, OrderCreatedKafkaEvent.class);
            
            log.info("✅ OrderCreatedKafkaEvent 파싱 성공:");
            log.info("  - OrderId: {}", orderEvent.getOrderNo());
            log.info("  - UserId: {}", orderEvent.getUserId());
            log.info("  - TotalAmount: {}", orderEvent.getTotalAmount());
            log.info("  - OrderItems: {}", orderEvent.getOrderItems());
            
            // 주문 관련 메트릭스 업데이트 (상품별 판매량 집계)
            if (orderEvent.getOrderItems() != null) {
                for (var orderItem : orderEvent.getOrderItems()) {
                    // 상품별 판매 메트릭스 업데이트
                    productMetricsService.updateSalesInfo(
                        orderItem.getProductId(),
                        orderItem.getUnitPrice().longValue() * orderItem.getQuantity(),
                        LocalDate.now()
                    );
                    
                    log.info("📊 판매 메트릭스 업데이트 - ProductId: {}, Amount: {}", 
                        orderItem.getProductId(), 
                        orderItem.getUnitPrice().longValue() * orderItem.getQuantity());
                }
            }
            
            log.info("✅ Order Created Event 처리 완료 - OrderId: {}", orderEvent.getOrderNo());
                
        } catch (Exception e) {
            log.error("❌ Order Created Event 처리 실패", e);
            throw e;
        }
    }
    
    /**
     * 주문 결제 완료 이벤트 처리
     */
    private void processOrderPaidEvent(String eventType, String aggregateId, String payload, String eventId) throws JsonProcessingException {
        log.info("💳 Order Paid Event 처리 - OrderId: {}", aggregateId);
        // TODO: 결제 완료 관련 비즈니스 로직 구현
        // 예: 배송 준비, 재고 확정, 포인트 적립 등
    }
    
    /**
     * 주문 취소 이벤트 처리
     */
    private void processOrderCancelledEvent(String eventType, String aggregateId, String payload, String eventId) throws JsonProcessingException {
        log.info("❌ Order Cancelled Event 처리 - OrderId: {}", aggregateId);
        // TODO: 주문 취소 관련 비즈니스 로직 구현
        // 예: 재고 복구, 포인트 환불, 쿠폰 복구 등
    }
}
