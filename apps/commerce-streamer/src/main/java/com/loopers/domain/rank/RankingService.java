package com.loopers.domain.rank;

import com.loopers.infrastructure.rank.RankingRedisRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {
    private final RankingRedisRepository rankingRedisRepository;

    /**
     * 랭킹 이벤트 처리
     * - 이벤트 타입에 따라 적절한 가중치와 점수를 적용하여 Redis ZSET에 누적
     */
    public void processRankingEvent(RankingEvent event) {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String rankingKey = "ranking:all:" + today;

            // 최종 점수 계산: weight * score
            double finalScore = event.getWeight() * event.getScore();

            // Redis ZSET에 점수 누적 (기존 점수에 추가)
            rankingRedisRepository.incrementScore(rankingKey, event.getProductId(), finalScore);

            log.debug("랭킹 이벤트 처리 완료 - productId: {}, eventType: {}, weight: {}, score: {}, finalScore: {}",
                event.getProductId(), event.getEventType(), event.getWeight(), event.getScore(), finalScore);

        } catch (Exception e) {
            log.error("랭킹 이벤트 처리 실패 - event: {}", event, e);
            throw new RuntimeException("랭킹 이벤트 처리 실패", e);
        }
    }

}
