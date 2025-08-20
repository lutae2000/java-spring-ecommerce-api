package com.loopers.application.like;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class LikeServiceConcurrentTest {
    private static final Logger log = LoggerFactory.getLogger(LikeServiceConcurrentTest.class);

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String PRODUCT_ID = "A0001";
    private static final int THREAD_COUNT = 50;

    @BeforeEach
    public void setup() {
        // Create users
        for (int i = 0; i < THREAD_COUNT; i++) {
            userRepository.save(
                User.builder()
                    .userId("user" + i)
                    .email("user" + i + "@naver.com")
                    .birthday("2000-01-01")
                    .gender(Gender.M)
                    .build()
            );
        }

        // Create product
        Product product = Product.builder()
            .code(PRODUCT_ID)
            .brand("B0001")
            .price(BigDecimal.valueOf(1000))
            .name("테스트 물품")
            .category1("ELECTRIC")
            .useYn(true)
            .build();

        productRepository.save(product);
    }

    @AfterEach
    void terminateEachTest() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("비관적 락 동시성 테스트")
    class PessimisticLockConcurrencyTest {

        @DisplayName("50명이 동시에 같은 상품에 좋아요를 눌렀을 때 비관적 락으로 데이터 정합성 보장")
        @Test
        @Transactional
        void concurrentLikeCreationWithPessimisticLock() throws InterruptedException {
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // Create like operations concurrently
            for (int i = 0; i < THREAD_COUNT; i++) {
                int userId = i;
                executorService.submit(() -> {
                    try {
                        // 각 스레드에서 별도 트랜잭션으로 실행
                        likeService.like("user" + userId, PRODUCT_ID);
                        successCount.incrementAndGet();
                        log.debug("Pessimistic lock like success for user{}", userId);
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        log.warn("Pessimistic lock like failed for user{}: {}", userId, e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executorService.shutdown();

            // Verify final state
            LikeSummary finalSummary = likeService.likeSummaryByProductId(PRODUCT_ID);
            log.info("Pessimistic lock test completed - Success: {}, Failure: {}, Final like count: {}", 
                successCount.get(), failureCount.get(), finalSummary.getLikesCount());

            // 비관적 락은 성공률이 높아야 함
            assertThat(successCount.get()).isGreaterThan(THREAD_COUNT * 8 / 10); // 80% 이상 성공
            assertThat(finalSummary.getLikesCount()).isEqualTo(successCount.get());
        }

        @DisplayName("동일 사용자가 동시에 같은 상품에 좋아요를 눌렀을 때 중복 방지")
        @Test
        @Transactional
        void sameUserConcurrentLikeWithPessimisticLock() throws InterruptedException {
            String userId = "user1";
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // Same user tries to like the same product multiple times concurrently
            for (int i = 0; i < THREAD_COUNT; i++) {
                executorService.submit(() -> {
                    try {
                        likeService.like(userId, PRODUCT_ID);
                        successCount.incrementAndGet();
                    } catch (DataIntegrityViolationException e) {
                        // Expected for duplicate attempts
                        log.debug("Duplicate like prevented: {}", e.getMessage());
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        log.warn("Unexpected error: {}", e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executorService.shutdown();

            // Verify only one like was created
            LikeSummary finalSummary = likeService.likeSummaryByProductId(PRODUCT_ID);
            log.info("Same user pessimistic lock test completed - Success: {}, Failure: {}, Final like count: {}", 
                successCount.get(), failureCount.get(), finalSummary.getLikesCount());

            assertThat(finalSummary.getLikesCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("낙관적 락 동시성 테스트")
    class OptimisticLockConcurrencyTest {

        @DisplayName("50명이 동시에 같은 상품에 좋아요를 눌렀을 때 낙관적 락으로 데이터 정합성 보장")
        @Test
        @Transactional
        void concurrentLikeCreationWithOptimisticLock() throws InterruptedException {
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            AtomicInteger optimisticFailureCount = new AtomicInteger(0);

            // Create like operations concurrently
            for (int i = 0; i < THREAD_COUNT; i++) {
                int userId = i;
                executorService.submit(() -> {
                    try {
                        likeService.likeOptimistic("user" + userId, PRODUCT_ID);
                        successCount.incrementAndGet();
                        log.debug("Optimistic lock like success for user{}", userId);
                    } catch (ObjectOptimisticLockingFailureException e) {
                        optimisticFailureCount.incrementAndGet();
                        log.debug("Optimistic lock failure for user{}: {}", userId, e.getMessage());
                    } catch (DataIntegrityViolationException e) {
                        // 중복 키 에러는 성공으로 간주 (이미 좋아요가 존재함)
                        successCount.incrementAndGet();
                        log.debug("Duplicate like for user{}: {}", userId, e.getMessage());
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        log.warn("Optimistic lock like failed for user{}: {}", userId, e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executorService.shutdown();

            // Verify final state
            LikeSummary finalSummary = likeService.likeSummaryByProductId(PRODUCT_ID);
            log.info("Optimistic lock test completed - Success: {}, Optimistic failures: {}, Other failures: {}, Final like count: {}", 
                successCount.get(), optimisticFailureCount.get(), failureCount.get(), finalSummary.getLikesCount());

            // 낙관적 락은 실패가 발생할 수 있지만, 최종 결과는 일관되어야 함
            assertThat(successCount.get()).isGreaterThan(0);
            assertThat(finalSummary.getLikesCount()).isEqualTo(successCount.get());
        }

        @DisplayName("동일 사용자가 동시에 같은 상품에 좋아요를 눌렀을 때 중복 방지 (낙관적 락)")
        @Test
        @Transactional
        void sameUserConcurrentLikeWithOptimisticLock() throws InterruptedException {
            String userId = "user1";
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // Same user tries to like the same product multiple times concurrently
            for (int i = 0; i < THREAD_COUNT; i++) {
                executorService.submit(() -> {
                    try {
                        likeService.likeOptimistic(userId, PRODUCT_ID);
                        successCount.incrementAndGet();
                    } catch (DataIntegrityViolationException e) {
                        // Expected for duplicate attempts
                        log.debug("Duplicate like prevented (optimistic): {}", e.getMessage());
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        log.warn("Unexpected error (optimistic): {}", e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executorService.shutdown();

            // Verify only one like was created
            LikeSummary finalSummary = likeService.likeSummaryByProductId(PRODUCT_ID);
            log.info("Same user optimistic lock test completed - Success: {}, Failure: {}, Final like count: {}", 
                successCount.get(), failureCount.get(), finalSummary.getLikesCount());

            assertThat(finalSummary.getLikesCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("비관적 락 vs 낙관적 락 비교 테스트")
    class LockComparisonTest {

        @DisplayName("비관적 락과 낙관적 락의 성능 비교")
        @Test
        @Transactional
        void comparePessimisticVsOptimisticLock() throws InterruptedException {
            // 비관적 락 테스트
            long pessimisticStartTime = System.currentTimeMillis();
            AtomicInteger pessimisticSuccess = new AtomicInteger(0);
            AtomicInteger pessimisticFailure = new AtomicInteger(0);
            
            ExecutorService pessimisticExecutor = Executors.newFixedThreadPool(10);
            CountDownLatch pessimisticLatch = new CountDownLatch(THREAD_COUNT);
            
            for (int i = 0; i < THREAD_COUNT; i++) {
                int userId = i;
                pessimisticExecutor.submit(() -> {
                    try {
                        likeService.like("user" + userId, PRODUCT_ID + "_pessimistic");
                        pessimisticSuccess.incrementAndGet();
                    } catch (Exception e) {
                        pessimisticFailure.incrementAndGet();
                    } finally {
                        pessimisticLatch.countDown();
                    }
                });
            }
            
            pessimisticLatch.await(30, TimeUnit.SECONDS);
            pessimisticExecutor.shutdown();
            long pessimisticEndTime = System.currentTimeMillis();
            
            // 낙관적 락 테스트
            long optimisticStartTime = System.currentTimeMillis();
            AtomicInteger optimisticSuccess = new AtomicInteger(0);
            AtomicInteger optimisticFailure = new AtomicInteger(0);
            
            ExecutorService optimisticExecutor = Executors.newFixedThreadPool(10);
            CountDownLatch optimisticLatch = new CountDownLatch(THREAD_COUNT);
            
            for (int i = 0; i < THREAD_COUNT; i++) {
                int userId = i;
                optimisticExecutor.submit(() -> {
                    try {
                        likeService.likeOptimistic("user" + userId, PRODUCT_ID + "_optimistic");
                        optimisticSuccess.incrementAndGet();
                    } catch (Exception e) {
                        optimisticFailure.incrementAndGet();
                    } finally {
                        optimisticLatch.countDown();
                    }
                });
            }
            
            optimisticLatch.await(30, TimeUnit.SECONDS);
            optimisticExecutor.shutdown();
            long optimisticEndTime = System.currentTimeMillis();
            
            // 결과 출력
            long pessimisticTime = pessimisticEndTime - pessimisticStartTime;
            long optimisticTime = optimisticEndTime - optimisticStartTime;
            
            log.info("=== Lock Performance Comparison ===");
            log.info("Pessimistic Lock - Time: {}ms, Success: {}, Failure: {}", 
                pessimisticTime, pessimisticSuccess.get(), pessimisticFailure.get());
            log.info("Optimistic Lock - Time: {}ms, Success: {}, Failure: {}", 
                optimisticTime, optimisticSuccess.get(), optimisticFailure.get());
            
            // 검증
            LikeSummary pessimisticSummary = likeService.likeSummaryByProductId(PRODUCT_ID + "_pessimistic");
            LikeSummary optimisticSummary = likeService.likeSummaryByProductId(PRODUCT_ID + "_optimistic");
            
            assertThat(pessimisticSummary.getLikesCount()).isEqualTo(pessimisticSuccess.get());
            assertThat(optimisticSummary.getLikesCount()).isEqualTo(optimisticSuccess.get());
        }
    }
}
