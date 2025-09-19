package com.loopers.infrastructure.rank;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RankingRedisRepository {
    private final StringRedisTemplate redisTemplate;

    /**
     * ZSET에 점수 누적 (기존 점수에 추가)
     */
    public void incrementScore(String key, String member, double score) {
        try {
            Double newScore = redisTemplate.opsForZSet().incrementScore(key, member, score);

            // TTL 설정 (2일)
            redisTemplate.expire(key, Duration.ofDays(2));

            log.debug("ZSET 점수 누적 완료 - key: {}, member: {}, addedScore: {}, newScore: {}",
                key, member, score, newScore);

        } catch (Exception e) {
            log.error("ZSET 점수 누적 실패 - key: {}, member: {}, score: {}", key, member, score, e);
            throw new RuntimeException("ZSET 점수 누적 실패", e);
        }
    }
}
