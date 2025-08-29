package com.loopers.domain.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.domain.like.event.LikeEvent;
import com.loopers.utils.DatabaseCleanUp;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Slf4j
@RecordApplicationEvents
@TestPropertySource(properties = {
    "spring.task.execution.pool.core-size=10",
    "spring.task.execution.pool.max-size=20", 
    "spring.task.execution.pool.queue-capacity=100"
})
class LikeServiceTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeSummaryRepository likeSummaryRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final String USER_ID = "test-user";
    private static final String PRODUCT_ID = "test-product";

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        databaseCleanUp.truncateAllTables();
    }

    @AfterEach
    void tearDown() {
        // 비동기 이벤트 처리를 위한 대기 시간 증가
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 테스트 데이터 정리
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("좋아요 생성 - 이벤트 기반 비동기 처리")
    @Transactional
    void like_ShouldCreateLikeAndPublishEvent() {
        // when
        likeService.like(USER_ID, PRODUCT_ID);

        // then - Like 엔티티가 생성되었는지 확인
        assertThat(likeService.likeExist(USER_ID, PRODUCT_ID)).isTrue();

        // then - 이벤트가 발행되었는지 확인
        assertThat(applicationEvents.stream(LikeEvent.class))
            .hasSize(1)
            .first()
            .satisfies(event -> {
                assertThat(event.getProductId()).isEqualTo(PRODUCT_ID);
                assertThat(event.getUserId()).isEqualTo(USER_ID);
                assertThat(event.isIncrement()).isTrue();
            });

        // then - LikeSummary가 비동기적으로 업데이트되는지 확인 (트랜잭션 관리 포함)
        // 비동기 처리가 완료될 때까지 잠시 대기
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 직접 조회하여 확인
        Long count = likeSummaryRepository.LikeSummaryCountByProductId(PRODUCT_ID);
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("좋아요 취소 - 이벤트 기반 비동기 처리")
    @Transactional
    void likeCancel_ShouldRemoveLikeAndPublishEvent() {
        // given - 먼저 좋아요 생성
        likeService.like(USER_ID, PRODUCT_ID);
        
        // 비동기 처리 대기
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 직접 조회하여 확인
        Long count = likeSummaryRepository.LikeSummaryCountByProductId(PRODUCT_ID);
        assertThat(count).isEqualTo(1L);

        // when
        likeService.likeCancel(USER_ID, PRODUCT_ID);

        // then - Like 엔티티가 삭제되었는지 확인
        assertThat(likeService.likeExist(USER_ID, PRODUCT_ID)).isFalse();

        // then - 취소 이벤트가 발행되었는지 확인
        assertThat(applicationEvents.stream(LikeEvent.class))
            .hasSize(2) // 생성 + 취소
            .extracting(LikeEvent::isIncrement)
            .containsExactly(true, false);

        // then - LikeSummary가 비동기적으로 감소되는지 확인
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 직접 조회하여 확인
        Long finalCount = likeSummaryRepository.LikeSummaryCountByProductId(PRODUCT_ID);
        assertThat(finalCount).isEqualTo(0L);
    }

    @Test
    @DisplayName("중복 좋아요 - 멱등성 보장")
    @Transactional
    void like_WhenDuplicate_ShouldBeIdempotent() {
        // given
        likeService.like(USER_ID, PRODUCT_ID);

        // when - 중복 좋아요 시도
        likeService.like(USER_ID, PRODUCT_ID);

        // then - 예외가 발생하지 않아야 함 (멱등성 보장)
        // then - 이벤트는 한 번만 발행되어야 함
        assertThat(applicationEvents.stream(LikeEvent.class)).hasSize(1);
        
        // then - LikeSummary가 여전히 1인지 확인
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Long count = likeSummaryRepository.LikeSummaryCountByProductId(PRODUCT_ID);
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("동시 좋아요 처리 - 이벤트 기반 eventual consistency")
    @Transactional
    void concurrentLikes_ShouldHandleEventualConsistency() {
        // given
        String user1 = "user1";
        String user2 = "user2";
        String user3 = "user3";

        // when - 동시에 여러 사용자가 좋아요
        likeService.like(user1, PRODUCT_ID);
        likeService.like(user2, PRODUCT_ID);
        likeService.like(user3, PRODUCT_ID);

        // then - 모든 Like 엔티티가 생성되었는지 확인
        assertThat(likeService.likeExist(user1, PRODUCT_ID)).isTrue();
        assertThat(likeService.likeExist(user2, PRODUCT_ID)).isTrue();
        assertThat(likeService.likeExist(user3, PRODUCT_ID)).isTrue();

        // then - 모든 이벤트가 발행되었는지 확인
        assertThat(applicationEvents.stream(LikeEvent.class)).hasSize(3);

        // then - LikeSummary가 최종적으로 올바른 값으로 수렴하는지 확인 (eventual consistency)
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 직접 조회하여 확인
        Long totalCount = likeSummaryRepository.LikeSummaryCountByProductId(PRODUCT_ID);
        assertThat(totalCount).isEqualTo(3L);
    }

    @Test
    @DisplayName("존재하지 않는 좋아요 취소 - 멱등성 보장")
    @Transactional
    void likeCancel_WhenNotExists_ShouldBeIdempotent() {
        // when - 존재하지 않는 좋아요 취소 시도
        likeService.likeCancel(USER_ID, PRODUCT_ID);

        // then - 예외가 발생하지 않아야 함 (멱등성 보장)
        // then - 이벤트가 발행되지 않아야 함
        assertThat(applicationEvents.stream(LikeEvent.class)).hasSize(0);
        
        // then - LikeSummary가 여전히 0인지 확인
        Long count = likeSummaryRepository.LikeSummaryCountByProductId(PRODUCT_ID);
        assertThat(count).isEqualTo(0L);
    }
}
