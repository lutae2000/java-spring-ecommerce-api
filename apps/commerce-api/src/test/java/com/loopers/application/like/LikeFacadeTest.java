package com.loopers.application.like;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.product.Product;
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
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    private ProductService productService;

    @BeforeEach
    public void setup(){
        User userBuilder = User.builder()
            .userId("utlee")
            .email("utlee@naver.com")
            .birthday("2000-01-01")
            .gender(Gender.M)
            .build();

        userRepository.save(userBuilder);

        Product product = Product.builder()
            .code("A0001")
            .brandCode("B0001")
            .price(BigDecimal.valueOf(1000))
            .name("테스트 물품")
            .category1("ELECTRIC")
            .useYn(true)
            .build();
        productService.createProduct(product);
    }

    @AfterEach
    void terminateEachTest() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("좋아요")
    class CreateLike{

        @DisplayName("성공")
        @ParameterizedTest
        @CsvSource({
            "utlee, A0001",
            "utlee, A0001"
        })
        void like(String userId, String productId){
            LikeResult likeResult = likeFacade.like(userId, productId);

            assertThat(likeResult.likesCount()).isEqualTo(1);
        }

        @DisplayName("미존재 물품으로 시도")
        @ParameterizedTest
        @CsvSource({
            "utlee, A0003",
            "utlee, A0004"
        })
        void like_unknown_product(String userId, String productId){


            CoreException response = Assert.assertThrows(CoreException.class, () -> {
                LikeResult likeResult = likeFacade.like(userId, productId);
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
                LikeResult likeResult = likeFacade.like(userId, productId);
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(response.getMessage()).isEqualTo("존재하는 회원이 없습니다");
        }
    }



    @Test
    public LikeResult likeCancel(String userId, String productId){
        return likeFacade.likeCancel(userId, productId);
    }
}
