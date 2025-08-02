package com.loopers.domain.product;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ProductTest {


    @Nested
    @DisplayName("상품 생성")
    class Create{

        @DisplayName("상품 정상적으로 생성")
        @ParameterizedTest
        @CsvSource({
            "ABC, 상품1, 10000, 10, , none, nike, shoes,  ,  ,true , ",
            "CDEFG, 상품2, 20000, 10, , none, t-shirts, ,  ,  ,false , ",
            "C1, 상품3, 30000, 10, , none, adidas, shoes,  ,  ,true , ",
        })
        void createProduct_succeed(String code, String name, BigDecimal price, Long quantity, String imgURL,
            String description, String brandCode, String category1, String category2, String category3,
            boolean useYn, Long like){

            //given

            //when
            Product product = new Product(code, name, price, quantity, imgURL, description, brandCode, category1, category2, category3, useYn, like);

            //then
            assert product.getCode().equals(code);
            assert product.getName().equals(name);
        }

        @DisplayName("상품코드 누락시 400 에러")
        @ParameterizedTest
        @CsvSource({
            ", 상품1, 10000, 10, , none, nike, shoes,  ,  ,true , ",
            ", 상품2, 20000, 10, , none, t-shirts, ,  ,  ,false , ",
            ", 상품3, 30000, 10, , none, adidas, shoes,  ,  ,true , ",
        })
        void createProduct_fail(String code, String name, BigDecimal price, Long quantity, String imgURL,
            String description, String brandCode, String category1, String category2, String category3,
            boolean useYn, Long like){
            //given

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                Product product = new Product(code, name, price, quantity, imgURL, description, brandCode, category1, category2, category3, useYn, like);
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("상품 코드는 필수값 입니다");
        }

    }
}
