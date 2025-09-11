package com.loopers.domain.rank;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 일간 랭킹정보 가져오기
     * @param date
     * @param page
     * @param size
     * @return
     */
    public RankingPage getDailyRanking(String date, int page, int size){
        String key = String.format("ranking:%s:%d:%d", date, page, size);

        long start = (long) (page - 1) * size;
        long end = start + size - 1;

        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        Set<TypedTuple<String>> tuples = zset.reverseRangeWithScores(key, start, end);
        long total = Optional.ofNullable(zset.zCard(key)).orElse(0L);

        List<ProductRanking> items = new ArrayList<>();
        if (tuples != null) {
            long rank = start + 1;
            for (TypedTuple<String> t : tuples) {
                String productId = t.getValue();
                Double like = t.getScore();
                items.add(new ProductRanking(productId, like, rank));
            }
        }
        return new RankingPage(key, page, size, total, items);
    }

    /**
     * 랭킹정보 가져오기
     * @param productId
     * @param date
     * @return
     */
    public Optional<ProductRanking> getProductRanking(String date, String productId) {
        String key = "ranking:all:" + (date == null || date.isBlank()
            ? DAY_FMT.format(LocalDate.now())
            : date);

        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        Long rank = zset.reverseRank(key, productId);
        Double score = zset.score(key, productId);

        if (rank == null)
            return Optional.empty();
        return Optional.of(new ProductRanking(productId, score != null ? score : 0.0, rank));
    }

}
