package com.loopers.domain.like;


import static org.assertj.core.api.Assertions.assertThat;


import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.like.LikeSummaryJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
public class LikeServiceTest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    LikeService likeService;

    @Autowired
    LikeSummaryJpaRepository likeSummaryJpaRepository;

    @BeforeAll
    void setup() {
        // 공통 데이터 삽입 (테스트 전체에서 사용)
        likeService.like("utlee", "shoes");
        likeService.like("utlee", "shirts");

        likeSummaryJpaRepository.save(new LikeSummary("shoes", 1L));
        likeSummaryJpaRepository.save(new LikeSummary("shirts", 1L));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("좋아요 생성")
    class Create {

        @DisplayName("여러 상품에 대해 좋아요 성공")
        @ParameterizedTest
        @CsvSource({
            "utlee, pants",
            "utlee, phone",
            "unicorn, beer",
            "unicorn, AOC"
        })
        void CreateLike(String loginId, String code) {
            likeService.like(loginId, code);
            List<Like> likeCount = likeService.getLikeByProductId(code);
            assertThat(likeCount.size()).isEqualTo(1);
        }

        @DisplayName("멱등성 적용")
        @ParameterizedTest
        @CsvSource({
            "utlee, shoes",
            "utlee, shoes",
            "unicorn, beer",
            "unicorn, beer"
        })
        void CreateLike_when_Failed_duplicate(String loginId, String code) {
            likeService.like(loginId, code);
            List<Like> likeCount = likeService.getLikeByProductId(code);
            assertThat(likeCount.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("좋아요 취소")
    class Cancel {

        @DisplayName("좋아요가 없는 상태에서 취소 성공")
        @ParameterizedTest
        @CsvSource({
            "utlee, pants",
            "utlee, phone",
            "unicorn, beer",
            "unicorn, AOC"
        })
        void CreateLike(String loginId, String code) {
            likeService.likeCancel(loginId, code);
            LikeSummary likeSummary = new LikeSummary(code, -1L);
            likeSummaryJpaRepository.save(likeSummary);

            List<Like> likeCount = likeService.getLikeByProductId(code);

            assertThat(likeCount.size()).isEqualTo(0);
        }
    }

    @DisplayName("좋아요 후 총 카운트")
    @Test
    void CreateLikeAndCancel() {

        likeService.like("utlee", "pasta");
        likeService.like("utlee", "phone");
        likeService.like("unicorn", "pasta");
        likeService.like("AOC", "coke");
        likeService.like("park", "phone");
        likeService.like("anonymous", "phone");
        likeService.likeCancel("anonymous", "phone");   //anonymous 유저가 취소


        assertThat(likeService.getLikeByProductId("phone").size()).isEqualTo(1);
    }

}
