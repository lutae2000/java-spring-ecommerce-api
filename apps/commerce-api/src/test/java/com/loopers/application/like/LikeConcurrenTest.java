package com.loopers.application.like;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeSummaryRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LikeConcurrenTest {

    @Autowired
    LikeFacade likeFacade;

    @Autowired
    LikeSummaryRepository likeSummaryRepository;


    @Autowired
    UserRepository userRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setup(){

        for( int i = 0; i <= 100; i++){
            //유저 100개 생성
            userRepository.save(
                User.builder()
                .userId("user" + i)
                .email("user@naver.com")
                .birthday("2000-01-01")
                .gender(Gender.M)
                .build()
            );
        }

        //물품 생성
        Product product = Product.builder()
            .code("A0001")
            .brandCode("B0001")
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
    @DisplayName("좋아요 동시성 테스트")
    class Create{

        @DisplayName("동일 상품에 대해 여러명이 좋아요 싫어요 요청하고 갯수 정상 반영")
        @Test
        void likeCreateSucceed() throws InterruptedException{
            int threadCount = 100;
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                int num = i;
                executorService.submit(() -> {
                    try {
                        String userId = "user" + num;
                        likeFacade.like(new LikeCriteria(userId, "A0001"));
                    } catch (Exception e) {
                        System.out.println("실패: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(); // 모든 스레드가 끝날 때까지 기다림
            executorService.shutdown();

            Long likeCount = likeFacade.likeSummaryCount("A0001");
            assertThat(likeCount).isEqualTo(threadCount);
        }

        @DisplayName("병렬 요청으로 동일상품에 대해 좋아요 테스트")
        @Test
        void completableFuture_test_likeCreate_when_same_product_and_same_user() throws InterruptedException{

            ExecutorService executor = Executors.newFixedThreadPool(100);
            List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> {
                    String userId = "user" + i;
                    return CompletableFuture.runAsync(() -> {
                        likeFacade.like(new LikeCriteria(userId, "A0001"));
                    });
                }).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            // 검증: 좋아요 수가 100개인지 확인
            Long likeCount = likeFacade.likeSummaryCount("A0001");
            assertThat(likeCount).isEqualTo(100);

            executor.shutdown();
        }
    }

    @DisplayName("동일 회원이 동시에 좋아요 호출하는 경우")
    @Test
    void likeCreate_when_same_user_and_same_product(){
        String userId = "user1";
        String productId = "A0001";
        ExecutorService executor = Executors.newFixedThreadPool(100);

        List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, 100)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                likeFacade.like(new LikeCriteria(userId, productId));
            }))
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 검증: 좋아요 수가 100개인지 확인
        Long likeCount = likeFacade.likeSummaryCount("A0001");
        assertThat(likeCount).isEqualTo(1);

        executor.shutdown();
    }

}
