package com.loopers.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 디버깅용 간단한 Consumer
 * 받은 메시지를 그대로 로깅하여 문제 진단
 */
@Component
@Slf4j
public class DebugEventConsumer {

    @KafkaListener(
        topics = "catalog-event",  // 하드코딩으로 테스트
        groupId = "debug-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void debugCatalogEvent(
            @Payload String rawMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "kafka_receivedPartitionId", required = false) Object partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("=== DEBUG Consumer ===");
            log.info("Topic: {}", topic);
            log.info("Partition: {}", partition);
            log.info("Offset: {}", offset);
            log.info("Raw Message Type: {}", rawMessage != null ? rawMessage.getClass().getName() : "null");
            log.info("Raw Message: {}", rawMessage);
            log.info("Message Length: {}", rawMessage != null ? rawMessage.length() : 0);
            
            // 메시지가 JSON인지 확인
            if (rawMessage != null && rawMessage.startsWith("{")) {
                log.info("✅ JSON 형태의 메시지 수신");
            } else {
                log.warn("⚠️ JSON이 아닌 메시지 수신");
            }
            
            acknowledgment.acknowledge();
            log.info("✅ Debug Consumer ACK 완료");
            
        } catch (Exception e) {
            log.error("❌ Debug Consumer 오류", e);
        }
    }
}
