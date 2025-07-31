package com.loopers.domain.product;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.point.PointEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ProductTest {
    @DisplayName("성공 - ")
    @ParameterizedTest
    @CsvSource({
        "ABC, 상품1, 10000, 10, , none, nike, shoes,  ,  ,true , ",
        "CDEFG, 상품2, 20000, 10, , none, t-shirts, ,  ,  ,false , ",
        "C1, 상품3, 30000, 10, , none, adidas, shoes,  ,  ,true , ",
    })
    void createProduct_succeed(String code, String name, BigDecimal price, int quantity, String imgURL,
                                String description, String brandCode, String category1, String category2, String category3,
                                boolean useYn, Long like){

        //given

        //when
        Product product = new Product(code, name, price, quantity, imgURL, description, brandCode, category1, category2, category3, useYn, like);

        //then
        assert product.getCode().equals(code);
        assert product.getName().equals(name);
    }

    @DisplayName("실패 - 0또는 마이너스 포인트 충전시 400에러 발생")
    @ParameterizedTest
    @CsvSource({
        "chicken, 0",
        "utlee, -10000",
        "test, -1000000",
        "player, -10000000"
    })
    void pointChargeFail(String loginId, Long pointValue){

        //when
        CoreException response = assertThrows(CoreException.class, () -> {
            PointEntity pointModel = new PointEntity(loginId, pointValue);
        });

        //then
        assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }
}
