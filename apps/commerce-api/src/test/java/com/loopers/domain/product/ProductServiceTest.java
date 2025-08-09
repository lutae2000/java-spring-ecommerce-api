package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)  // 인스
public class ProductServiceTest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    ProductService productService;

    @BeforeAll
    void setup(){
        Product initProduct = new Product("A0001", "상품1", BigDecimal.valueOf(10000), 10L, "https://naver.com/img", "상품에 대한 설명", "B0001", "ELECTRIC", null, null, true, 0L);

        ProductCommand productCommand = ProductCommand.toProduct(initProduct);
        productService.createProduct(productCommand);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @Nested
    @DisplayName("물품 조회")
    class GetProductInfo {

        @Test
        @DisplayName("성공")
        void inquiryPoint(){
            //given

            //when
            ProductInfo productInfo = productService.findProduct("A0001");


            //then
            assertAll(
                () -> assertThat(productInfo.getCode()).isEqualTo("A0001"),
                () -> assertThat(productInfo.getName()).isEqualTo("상품1"),
                () -> assertThat(productInfo.getQuantity()).isEqualTo(10L)
            );
        }

        @Test
        @DisplayName("실패 - 상품 미존재시 404에러")
        void inquiryPoint_when_not_exists_user(){

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                ProductInfo productInfo = productService.findProduct("A0002");
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
