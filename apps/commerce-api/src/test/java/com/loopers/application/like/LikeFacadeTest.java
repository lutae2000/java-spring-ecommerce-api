package com.loopers.application.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LikeFacadeTest {

    @Autowired
    LikeFacade likeFacade;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LikeService likeService;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setup(){
        User userBuilder1 = User.builder()
            .userId("utlee")
            .email("utlee@naver.com")
            .birthday("2000-01-01")
            .gender(Gender.M)
            .build();

        userRepository.save(userBuilder1);

        User userBuilder2 = User.builder()
            .userId("ant")
            .email("ant@naver.com")
            .birthday("2000-01-01")
            .gender(Gender.M)
            .build();

        userRepository.save(userBuilder2);


        // Product 생성 방식 수정
        Product product1 = Product.create(
            "A0001",
            "테스트 물품1",
            BigDecimal.valueOf(1000),
            10L,
            "image1.jpg",
            "테스트 물품1 설명",
            "ELECTRIC",
            "컴퓨터",
            "노트북"
        );
        productRepository.save(product1);

        Product product2 = Product.create(
            "A0002",
            "테스트 물품2",
            BigDecimal.valueOf(2000),
            10L,
            "image2.jpg",
            "테스트 물품2 설명",
            "ELECTRIC",
            "컴퓨터",
            "데스크톱"
        );
        productRepository.save(product2);
    }

    @AfterEach
    void terminateEachTest() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("좋아요")
    class CreateLike{

        @DisplayName("1번만 좋아요")
        @Test
        void like(){
            String userId = "utlee";
            String productId = "A0001";
            likeFacade.like(new LikeCriteria(userId, productId));
            Boolean like = likeFacade.likeExist(userId, productId);
            Long likeSummaryCount = likeFacade.likeSummaryCount(productId);

            assertAll(
                () -> assertThat(like).isEqualTo(Boolean.TRUE),
                () -> assertThat(likeSummaryCount).isEqualTo(1L)
            );
        }

        @DisplayName("동일 물품 좋아요 클릭")
        @Test
        void like_same_productID_and_userid(){
            String userId = "utlee";
            String productId = "A0001";

            likeFacade.like(new LikeCriteria(userId, productId));
            likeFacade.like(new LikeCriteria(userId, productId));

            Boolean like = likeService.likeExist(userId, productId);
            Long likeSummaryCount = likeFacade.likeSummaryCount(productId);

            assertAll(
                () -> assertThat(like).isEqualTo(Boolean.TRUE),
                () -> assertThat(likeSummaryCount).isEqualTo(1L)
            );
        }


        @DisplayName("다른 계정들이 좋아요")
        @ParameterizedTest
        @CsvSource({
            "utlee, A0001",
            "ant, A0001",
        })
        void like_count2(String userId, String productId){
            likeFacade.like(new LikeCriteria(userId, productId));

            Boolean like = likeFacade.likeExist(userId, productId);
            Long likeSummaryCount = likeFacade.likeSummaryCount(productId);

            assertAll(
                () -> assertThat(like).isEqualTo(Boolean.TRUE),
                () -> assertThat(likeSummaryCount).isEqualTo(1L)
            );
        }


        @DisplayName("미존재 물품으로 시도")
        @ParameterizedTest
        @CsvSource({
            "utlee, A0003",
            "utlee, A0004"
        })
        void like_unknown_product(String userId, String productId){


            CoreException response = Assert.assertThrows(CoreException.class, () -> {
                likeFacade.like(new LikeCriteria(userId, productId));
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(response.getMessage()).isEqualTo("검색하려는 물품이 없습니다");
        }


        @DisplayName("미존재 회원으로 시도")
        @ParameterizedTest
        @CsvSource({
            "unknown, A0001",
            "unknown, A0002"
        })
        void like_unknown(String userId, String productId){

            CoreException response = Assert.assertThrows(CoreException.class, () -> {
                likeFacade.like(new LikeCriteria(userId, productId));
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(response.getMessage()).isEqualTo("존재하는 회원이 없습니다");
        }
    }


    @Nested
    @DisplayName("좋아요 취소")
    class Cancel{

        @DisplayName("정상")
        @ParameterizedTest
        @CsvSource({
            "utlee, A0001",
            "ant, A0001"
        })
        void likeCancel(String userId, String productId){
             likeFacade.likeCancel(new LikeCriteria(userId, productId));
        }

        @DisplayName("실패 - 미존재 회원이 시도시 404에러")
        @ParameterizedTest
        @CsvSource({
            "user1, A0001",
            "user2, A0001"
        })
        void likeCancel_failed_not_exists_user(String userId, String productId){
            CoreException response = Assert.assertThrows(CoreException.class, () -> {
                likeFacade.likeCancel(new LikeCriteria(userId, productId));
            });
            assertThat(response.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

}
