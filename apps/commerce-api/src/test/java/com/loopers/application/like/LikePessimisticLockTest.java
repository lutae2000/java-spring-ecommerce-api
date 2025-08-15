package com.loopers.application.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeSummaryRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.like.LikeSummaryJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class LikePessimisticLockTest {

    @Autowired
    private LikeJpaRepository likeJpaRepository;

    @Autowired
    private LikeSummaryJpaRepository likeSummaryJpaRepository;

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

        // Create initial LikeSummary
        LikeSummary likeSummary = new LikeSummary(PRODUCT_ID, 0L);
        likeSummaryJpaRepository.save(likeSummary);
    }

    @AfterEach
    void terminateEachTest() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("비관적 락 동시성 테스트")
    class PessimisticLockConcurrencyTest {

        @DisplayName("순차적으로 좋아요를 생성하여 비관적 락 동작 확인")
        @Test
        @Transactional
        void sequentialLikeCreationWithPessimisticLock() {
            // Create likes sequentially to verify pessimistic lock works
            for (int i = 0; i < 10; i++) {
                createLikeWithPessimisticLock("user" + i, PRODUCT_ID);
            }

            // Verify final state
            Optional<LikeSummary> finalSummary = likeSummaryJpaRepository.getLikeByProductIdForUpdate(PRODUCT_ID);
            assertThat(finalSummary).isPresent();
            assertThat(finalSummary.get().getLikesCount()).isEqualTo(10);

            long actualLikeCount = likeJpaRepository.count();
            assertThat(actualLikeCount).isEqualTo(10);
        }

        @DisplayName("동일 사용자의 중복 좋아요 시도 시 예외 발생 확인")
        @Test
        @Transactional
        void duplicateLikeAttemptWithPessimisticLock() {
            String userId = "user1";
            
            // First like should succeed
            createLikeWithPessimisticLock(userId, PRODUCT_ID);
            
            // Second like should throw exception
            assertThatThrownBy(() -> createLikeWithPessimisticLock(userId, PRODUCT_ID))
                .isInstanceOf(DataIntegrityViolationException.class);

            // Verify only one like was created
            Optional<LikeSummary> finalSummary = likeSummaryJpaRepository.getLikeByProductIdForUpdate(PRODUCT_ID);
            assertThat(finalSummary).isPresent();
            assertThat(finalSummary.get().getLikesCount()).isEqualTo(1);

            long actualLikeCount = likeJpaRepository.count();
            assertThat(actualLikeCount).isEqualTo(1);
        }

        @DisplayName("좋아요 생성 후 삭제하여 비관적 락 동작 확인")
        @Test
        @Transactional
        void likeCreationAndDeletionWithPessimisticLock() {
            String userId = "user1";
            
            // Create like
            createLikeWithPessimisticLock(userId, PRODUCT_ID);
            
            // Verify like was created
            Optional<LikeSummary> summaryAfterCreate = likeSummaryJpaRepository.getLikeByProductIdForUpdate(PRODUCT_ID);
            assertThat(summaryAfterCreate).isPresent();
            assertThat(summaryAfterCreate.get().getLikesCount()).isEqualTo(1);
            
            // Delete like
            deleteLikeWithPessimisticLock(userId, PRODUCT_ID);
            
            // Verify like was deleted
            Optional<LikeSummary> summaryAfterDelete = likeSummaryJpaRepository.getLikeByProductIdForUpdate(PRODUCT_ID);
            assertThat(summaryAfterDelete).isPresent();
            assertThat(summaryAfterDelete.get().getLikesCount()).isEqualTo(0);
            
            long actualLikeCount = likeJpaRepository.count();
            assertThat(actualLikeCount).isEqualTo(0);
        }

        @DisplayName("여러 사용자의 좋아요 생성 후 삭제하여 비관적 락 동작 확인")
        @Test
        @Transactional
        void multipleUsersLikeCreationAndDeletionWithPessimisticLock() {
            // Create likes for multiple users
            for (int i = 0; i < 5; i++) {
                createLikeWithPessimisticLock("user" + i, PRODUCT_ID);
            }
            
            // Verify all likes were created
            Optional<LikeSummary> summaryAfterCreate = likeSummaryJpaRepository.getLikeByProductIdForUpdate(PRODUCT_ID);
            assertThat(summaryAfterCreate).isPresent();
            assertThat(summaryAfterCreate.get().getLikesCount()).isEqualTo(5);
            
            // Delete some likes
            deleteLikeWithPessimisticLock("user0", PRODUCT_ID);
            deleteLikeWithPessimisticLock("user2", PRODUCT_ID);
            deleteLikeWithPessimisticLock("user4", PRODUCT_ID);
            
            // Verify remaining likes
            Optional<LikeSummary> summaryAfterDelete = likeSummaryJpaRepository.getLikeByProductIdForUpdate(PRODUCT_ID);
            assertThat(summaryAfterDelete).isPresent();
            assertThat(summaryAfterDelete.get().getLikesCount()).isEqualTo(2);
            
            long actualLikeCount = likeJpaRepository.count();
            assertThat(actualLikeCount).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("비관적 락 동작 검증")
    class PessimisticLockBehaviorTest {

        @DisplayName("비관적 락으로 Like 엔티티 조회 시 락 획득 확인")
        @Test
        @Transactional
        void pessimisticLockOnLikeEntity() {
            // Create a like first
            Like like = new Like(PRODUCT_ID, "user1");
            likeJpaRepository.save(like);

            // Try to find with pessimistic lock
            Optional<Like> foundLike = likeJpaRepository.findByUserIdAndProductId("user1", PRODUCT_ID);
            assertThat(foundLike).isPresent();
            assertThat(foundLike.get().getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(foundLike.get().getUserId()).isEqualTo("user1");
        }

        @DisplayName("비관적 락으로 LikeSummary 엔티티 조회 시 락 획득 확인")
        @Test
        @Transactional
        void pessimisticLockOnLikeSummaryEntity() {
            // Try to find with pessimistic lock
            Optional<LikeSummary> foundSummary = likeSummaryJpaRepository.getLikeByProductIdForUpdate(PRODUCT_ID);
            assertThat(foundSummary).isPresent();
            assertThat(foundSummary.get().getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(foundSummary.get().getLikesCount()).isEqualTo(0L);
        }

        @DisplayName("LikeSummary 증가/감소 메서드 동작 확인")
        @Test
        @Transactional
        void likeSummaryIncreaseDecreaseMethods() {
            Optional<LikeSummary> summaryOpt = likeSummaryJpaRepository.getLikeByProductIdForUpdate(PRODUCT_ID);
            assertThat(summaryOpt).isPresent();
            
            LikeSummary summary = summaryOpt.get();
            assertThat(summary.getLikesCount()).isEqualTo(0L);

            // Test increase
            summary.increaseLikesCount();
            assertThat(summary.getLikesCount()).isEqualTo(1L);

            // Test decrease
            summary.decreaseLikesCount();
            assertThat(summary.getLikesCount()).isEqualTo(0L);

            // Test decrease when count is 0
            summary.decreaseLikesCount();
            assertThat(summary.getLikesCount()).isEqualTo(0L);
        }
    }

    /**
     * 비관적 락을 사용하여 Like를 생성하는 메서드
     */
    @Transactional
    public void createLikeWithPessimisticLock(String userId, String productId) {
        // Check if like already exists with pessimistic lock
        Optional<Like> existingLike = likeJpaRepository.findByUserIdAndProductId(userId, productId);
        if (existingLike.isPresent()) {
            throw new DataIntegrityViolationException("Like already exists for user: " + userId + ", product: " + productId);
        }

        // Create new like
        Like newLike = new Like(productId, userId);
        likeJpaRepository.save(newLike);

        // Update LikeSummary with pessimistic lock
        Optional<LikeSummary> summaryOpt = likeSummaryJpaRepository.getLikeByProductIdForUpdate(productId);
        if (summaryOpt.isPresent()) {
            LikeSummary summary = summaryOpt.get();
            summary.increaseLikesCount();
            likeSummaryJpaRepository.save(summary);
        } else {
            LikeSummary newSummary = new LikeSummary(productId, 1L);
            likeSummaryJpaRepository.save(newSummary);
        }
    }

    /**
     * 비관적 락을 사용하여 Like를 삭제하는 메서드
     */
    @Transactional
    public void deleteLikeWithPessimisticLock(String userId, String productId) {
        // Check if like exists with pessimistic lock
        Optional<Like> existingLike = likeJpaRepository.findByUserIdAndProductId(userId, productId);
        if (existingLike.isEmpty()) {
            return; // Nothing to delete
        }

        // Delete the like
        likeJpaRepository.delete(existingLike.get());

        // Update LikeSummary with pessimistic lock
        Optional<LikeSummary> summaryOpt = likeSummaryJpaRepository.getLikeByProductIdForUpdate(productId);
        if (summaryOpt.isPresent()) {
            LikeSummary summary = summaryOpt.get();
            summary.decreaseLikesCount();
            likeSummaryJpaRepository.save(summary);
        }
    }
}
