package com.loopers.domain.rank;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankingService {
    private final StringRedisTemplate redisTemplate;
    private final ProductRepository productRepository;

    /**
     * 랭킹 조회 (페이지네이션)
     */
    public List<RankingResponseDto> getRanking(String date, int size, int page) {
        String key = "ranking:all:" + date;
        int start = (page - 1) * size;
        int end = start + size - 1;

        log.info("랭킹 조회 시작 - key: {}, start: {}, end: {}", key, start, end);

        // 1. Redis에서 랭킹(상품ID, 점수) 조회
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
            .reverseRangeWithScores(key, start, end);

        if (ObjectUtils.isEmpty(tuples)) {
            log.info("랭킹 데이터 없음 - key: {}", key);
            return Collections.emptyList();
        }

        // 2. 상품 ID 리스트 추출
        List<String> productIds = tuples.stream()
            .map(ZSetOperations.TypedTuple::getValue)
            .toList();

        log.info("랭킹 상품 ID 조회 - count: {}, productIds: {}", productIds.size(), productIds);

        // 3. ID 리스트를 사용해 DB에서 상품 정보 한 번에 조회 (IN 절 활용)
        Map<String, Product> productMap = productRepository.findProductByProductIdList(productIds).stream()
            .collect(Collectors.toMap(Product::getCode, product -> product));

        // 4. 랭킹 응답 DTO 생성
        List<RankingResponseDto> rankingList = new ArrayList<>();
        int rank = start + 1;

        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String productId = tuple.getValue();
            Double score = tuple.getScore();
            Product product = productMap.get(productId);

            if (product != null) {
                RankingResponseDto rankingDto = RankingResponseDto.builder()
                    .productId(productId)
                    .productName(product.getName())
                    .price(product.getPrice())
                    .brandName(product.getBrand())
                    .category(product.getCategory1())
                    .score(score)
                    .rank(rank)
                    .build();
                rankingList.add(rankingDto);
                rank++;
            } else {
                log.warn("상품 정보 없음 - productId: {}", productId);
            }
        }

        log.info("랭킹 조회 완료 - count: {}", rankingList.size());
        return rankingList;
    }

    /**
     * 오늘 날짜 기준 특정 상품의 랭킹 조회
     */
    public ProductRankInfo getTodayProductRank(String productId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return getProductRank(productId, today);
    }

    /**
     * 특정 상품의 랭킹 조회
     */
    public ProductRankInfo getProductRank(String productId, String date) {
        String key = "ranking:all:" + date;

        // Redis ZSET에서 해당 상품의 순위 조회
        Long rank = redisTemplate.opsForZSet().reverseRank(key, productId);

        if (rank == null) {
            log.info("상품이 랭킹에 없음 - productId: {}, date: {}", productId, date);
            return null;
        }

        // 점수도 함께 조회
        Double score = redisTemplate.opsForZSet().score(key, productId);

        return new ProductRankInfo(rank, ObjectUtils.isNotEmpty(score) ? score : 0L);
    }

    /**
     * 랭킹 키 생성
     */
    public static String generateRankingKey(String date) {
        return "ranking:all:" + date;
    }

    /**
     * 오늘 날짜 키 생성
     */
    public static String generateTodayRankingKey() {
        return generateRankingKey(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }
}
