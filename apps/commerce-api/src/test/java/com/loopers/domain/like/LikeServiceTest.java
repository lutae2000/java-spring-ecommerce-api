package com.loopers.domain.like;


import static org.assertj.core.api.Assertions.assertThat;


import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // 인스턴스 재사용
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
        @DisplayName("성공")
        @ParameterizedTest
        @CsvSource({
            "utlee, pants",
            "utlee, phone",
            "unicorn, beer",
            "unicorn, AOC"
        })
        void CreateLike(String loginId, String code) {
            LikeInfo likeInfo = likeService.unlike(loginId, code);

            assertThat(likeInfo.getLikesCount()).isNotNull();
        }
    }

}
