package com.loopers.consumer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingConsumer {
    private final RedisTemplate<String, String> redisTemplate;
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long TTL_SEC = 2 * 24 * 60 * 60;

    //나중에 DB에서 관리
    private static final double W_VIEW = 0.1;
    private static final double W_LIKE = 0.2;
    private static final double W_ORDER = 0.6;

    @KafkaListener(
        topics = {"catalog-events", "order-events"},
        containerFactory = "BATCH_LISTENER"
    )
    public void onBatch(List<ConsumerRecord<String, Object>> batch, Acknowledgment ack) {
        try {
            if (batch == null || batch.isEmpty()) {
                ack.acknowledge();
                return;
            }

            String key = dailyKey(LocalDate.now());
            Map<String, Double> productScoreDelta = new HashMap<>();

            for (ConsumerRecord<String, Object> record : batch) {
                Object payload = record.value();
                if (!(payload instanceof BaseKafkaEvent event)) continue;

                if (payload instanceof ProductViewEvent e) {    //조회했을때의 가중치
                    productScoreDelta.merge(e.getProductId(), W_VIEW * 1.0, Double::sum);

                } else if (payload instanceof ProductLikeChangedEvent e) {  //좋아요 했을때의 가중치
                    double delta = (e.isLiked() ? 1.0 : -1.0);
                    productScoreDelta.merge(e.getProductId(), W_LIKE * delta, Double::sum);

                } else if (payload instanceof OrderCreatedKafkaEvent e) {   //주문했을때의 가중치
                    double orderScore = e.getItems().stream()
                        .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .mapToDouble(BigDecimal::doubleValue)
                        .sum();
                    // 필요시 log 정규화: orderScore = Math.log1p(orderScore);
                    productScoreDelta.merge(e.getOrderId(), W_ORDER * orderScore, Double::sum);
                    // 만약 product 단위 반영이 필요하면 OrderItem별 productId에 누적하십시오.
                }
            }

            if (!productScoreDelta.isEmpty()) {
                ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
                for (Map.Entry<String, Double> entry : productScoreDelta.entrySet()) {
                    zset.incrementScore(key, entry.getKey(), entry.getValue());
                }
                // 키가 새로 만들어졌다면 TTL 설정
                redisTemplate.expire(key, java.time.Duration.ofSeconds(TTL_SEC));
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Ranking batch consume failed", e);
            // manual ack이므로 실패 시 재처리됨
        }
    }

    private String dailyKey(LocalDate date) {
        return "ranking:all:" + DAY_FMT.format(date);
    }
}
