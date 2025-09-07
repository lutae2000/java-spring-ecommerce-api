package com.loopers.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 상품 캐시 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 캐시 키 패턴
    private static final String PRODUCT_CACHE_PREFIX = "product:";

    /**
     * 특정 상품의 모든 관련 캐시 삭제
     */
    public void deleteProductCache(String productId) {
        try {
            log.info("상품 캐시 삭제 시작 - productId: {}", productId);

            String cacheKey = PRODUCT_CACHE_PREFIX + productId;
            redisTemplate.delete(cacheKey);

        } catch (Exception e) {
            log.error("상품 캐시 삭제 실패 - productId: {}, error: {}", productId, e.getMessage(), e);
            throw new RuntimeException("캐시 삭제 실패", e);
        }
    }
}
