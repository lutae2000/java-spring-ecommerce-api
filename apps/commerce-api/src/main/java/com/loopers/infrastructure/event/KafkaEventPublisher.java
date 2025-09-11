package com.loopers.infrastructure.event;

import com.loopers.message.KafkaEventMessage;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public <T> void publishEvent(String topic, String key, KafkaEventMessage<T> message) {
        log.info("publishEvent topic: {}, key: {}, event: {}", topic, key, message);
        CompletableFuture<SendResult<Object, Object>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if(ex == null){
                log.debug("Event published successfully - topic: {}, key: {}, message: {}", topic, key,message);
            } else {
                log.error("Event publishing failed - topic: {}, key: {}, message: {}", topic, key,message, ex);
            }
        });
    }
}
