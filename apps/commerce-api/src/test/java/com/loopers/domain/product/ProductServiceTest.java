package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;


import com.loopers.domain.brand.Brand;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProductServiceTest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductService productService;

    @Autowired
    BrandJpaRepository brandJpaRepository;

    @BeforeEach
    void setup(){
        // Brand 먼저 생성하고 저장
        Brand brand = Brand.builder()
            .code("B0001")
            .name("테스트 브랜드")
            .description("테스트 브랜드 설명")
            .imgURL("brand.jpg")
            .useYn(true)
            .build();

        // Brand를 먼저 저장
        Brand savedBrand = brandJpaRepository.save(brand);

        // Product 생성 - 저장된 Brand 객체 사용
        Product initProduct = Product.createWithBrand(
            "A0001",
            "상품1",
            BigDecimal.valueOf(10000),
            10L,
            "https://naver.com/img",
            "상품에 대한 설명",
            savedBrand,  // 저장된 Brand 객체 사용
            "ELECTRIC",
            "컴퓨터",
            "노트북"
        );

        productRepository.save(initProduct);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @Nested
    @DisplayName("물품 상세조회")
    class GetProductInfo {

        @Test
        @DisplayName("성공")
        void inquiryProduct(){
            //given

            //when
            ProductInfo productInfo = productService.findProduct("A0001");

            //then
            assertAll(
                () -> assertThat(productInfo.getCode()).isEqualTo("A0001"),
                () -> assertThat(productInfo.getName()).isEqualTo("상품1"),
                () -> assertThat(productInfo.getQuantity()).isEqualTo(10L),
                () -> assertThat(productInfo.getBrandCode()).isEqualTo("B0001")
            );
        }

        @Test
        @DisplayName("실패 - 상품 미존재시 404에러")
        void inquiryProduct_when_not_exists_product(){
            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                ProductInfo productInfo = productService.findProduct("A0002");
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("물품 생성")
    class CreateProduct {

        @Test
        @DisplayName("성공")
        void createProduct_success(){
            //given
            ProductCommand command = new ProductCommand(
                "A0003",
                "상품3",
                BigDecimal.valueOf(15000),
                5L,
                "https://naver.com/img3",
                "상품3에 대한 설명",
                "B0001",
                "ELECTRIC",
                "컴퓨터",
                "마우스"
            );

            //when
            productService.createProduct(command);

            //then
            Product savedProduct = productRepository.findProduct("A0003");
            assertThat(savedProduct).isNotNull();
            assertThat(savedProduct.getName()).isEqualTo("상품3");
            assertThat(savedProduct.getCode()).isEqualTo("A0003");
        }
    }
}
