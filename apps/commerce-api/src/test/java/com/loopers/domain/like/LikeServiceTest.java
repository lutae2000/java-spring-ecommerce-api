package com.loopers.domain.like;


import static org.assertj.core.api.Assertions.assertThat;


import com.loopers.utils.DatabaseCleanUp;
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

    @BeforeAll
    void setup() {
        // 공통 데이터 삽입 (테스트 전체에서 사용)
        likeService.like("utlee", "shoes");
        likeService.like("utlee", "shirts");
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
            LikeInfo likeInfo = likeService.like(loginId, code);

            assertThat(likeInfo.getLikesCount()).isNotNull();
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
            LikeInfo likeInfo = likeService.like(loginId, code);
            assertThat(likeInfo.getLikesCount()).isNotNull();
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
            LikeInfo likeInfo = likeService.likeCancel(loginId, code);

            assertThat(likeInfo.getLikesCount()).isEqualTo(0);
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
        likeService.likeCancel("anonymous", "phone");   //멱등 + anonymous 유저가 취소

        assertThat(likeService.countLike("phone")).isEqualTo(2);
    }

}
