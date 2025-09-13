package com.loopers.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.rank.RankingEvent;
import com.loopers.domain.rank.RankingService;
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
        @Payload List<String> messages,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("랭킹 이벤트 배치 처리 시작 - topic: {}, partition: {}, offset: {}, messageCount: {}",
            topic, partition, offset, messages.size());

        try {
            // 배치 메시지 파싱 및 처리
            for (String message : messages) {
                try {
                    RankingEvent event = objectMapper.readValue(message, RankingEvent.class);
                    rankingEventProcessingService.processRankingEvent(event);
                } catch (JsonProcessingException e) {
                    log.error("랭킹 이벤트 파싱 실패 - message: {}", message, e);
                }
            }

            // 수동 커밋
            acknowledgment.acknowledge();

            log.info("랭킹 이벤트 배치 처리 완료 - processedCount: {}", messages.size());

        } catch (Exception e) {
            log.error("랭킹 이벤트 배치 처리 실패 - topic: {}, partition: {}, offset: {}",
                topic, partition, offset, e);
            // 에러 발생 시에도 커밋 (재처리 방지)
            acknowledgment.acknowledge();
        }
    }

}
