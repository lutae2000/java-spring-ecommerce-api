package com.loopers.config.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheTemplate {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 캐시에서 Object 값을 조회하고, 없으면 supplier를 통해 생성하여 캐시에 저장
     */
    public <T> T getOrSet(String key, Class<T> clazz, Duration ttl, Supplier<T> supplier) {
        try {
            log.debug("getOrSet called - key: {}, class: {}", key, clazz.getSimpleName());

            // 1. 캐시에서 조회 시도
            Optional<T> cachedValue = get(key, clazz);
            if (cachedValue.isPresent()) {
                log.debug("Cache hit - returning cached value for key: {}", key);
                return cachedValue.get();
            }

            // 2. 캐시 미스 - supplier를 통해 값 생성
            log.debug("Cache miss - calling supplier for key: {}", key);
            T value = supplier.get();

            if (value != null) {
                log.debug("Setting cache - key: {}, value type: {}", key, value.getClass().getSimpleName());

                // 3. 캐시 저장 시도 (실패해도 서비스는 정상 동작)
                try {
                    boolean cacheSet = set(key, value, ttl);
                    if (cacheSet) {
                        log.debug("Cache set successfully for key: {}", key);
                    } else {
                        log.warn("Failed to set cache for key: {}", key);
                    }
                } catch (Exception cacheException) {
                    log.warn("Cache set failed for key: {}, but continuing with service: {}", key, cacheException.getMessage());
                }
            } else {
                log.warn("Supplier returned null for key: {}", key);
            }

            return value;

        } catch (Exception e) {
            log.error("Cache operation failed for key: {}, error: {}", key, e.getMessage(), e);
            // 캐시 실패 시에도 서비스는 정상 동작 - supplier 호출
            log.info("Falling back to supplier due to cache failure for key: {}", key);
            try {
                return supplier.get();
            } catch (Exception supplierException) {
                log.error("Supplier also failed for key: {}, error: {}", key, supplierException.getMessage(), supplierException);
                throw new RuntimeException("Both cache and supplier failed", supplierException);
            }
        }
    }

    /**
     * Object를 캐시에 저장 (성공/실패 반환)
     */
    public <T> boolean set(String key, T value, Duration ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttl);
            log.debug("Cache set successfully - key: {}, ttl: {}, json length: {}", key, ttl, jsonValue.length());
            return true;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize value for cache key: {}, value: {}", key, value, e);
            return false;
        } catch (Exception e) {
            log.error("Failed to set cache for key: {}, error: {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Object를 캐시에 저장 (TTL 없음)
     */
    public <T> boolean set(String key, T value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue);
            log.debug("Cache set - key: {}", key);
            return true;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize value for cache key: {}", key, e);
            return false;
        } catch (Exception e) {
            log.error("Failed to set cache for key: {}, error: {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 캐시에서 Object 값 조회
     */
    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            String cachedValue = redisTemplate.opsForValue().get(key);
            if (cachedValue != null) {
                T value = objectMapper.readValue(cachedValue, clazz);
                log.debug("Cache hit - key: {}", key);
                return Optional.of(value);
            }
            log.debug("Cache miss - key: {}", key);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cache for key: {}, error: {}", key, e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get cache for key: {}, error: {}", key, e.getMessage(), e);
            return Optional.empty();
        }
    }

    // ===== List Object 조회/저장 =====

    /**
     * List Object를 캐시에 저장
     */
    public <T> void setList(String key, List<T> value, Duration ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttl);
            log.debug("Cache set list - key: {}, size: {}, ttl: {}", key, value.size(), ttl);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize list for cache key: {}", key, e);
        }
    }

    /**
     * 캐시에서 List Object 조회
     */
    public <T> Optional<List<T>> getList(String key, Class<T> clazz) {
        try {
            String cachedValue = redisTemplate.opsForValue().get(key);
            if (cachedValue != null) {
                List<T> value = objectMapper.readValue(cachedValue,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
                log.debug("Cache hit list - key: {}", key);
                return Optional.of(value);
            }
            log.debug("Cache miss list - key: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to get list cache for key: {}, error: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 캐시 키 생성
     */
    public static String generateKey(String prefix, Object... params) {
        StringBuilder key = new StringBuilder(prefix);
        for (Object param : params) {
            key.append(":").append(param != null ? param.toString() : "null");
        }
        return key.toString();
    }

    /**
     * 캐시 키 생성 (타입 안전)
     */
    public static String generateKey(String prefix, Map<String, Object> params) {
        StringBuilder key = new StringBuilder(prefix);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            key.append(":").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return key.toString();
    }

    // 삭제
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
