package com.loopers.application.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.product.ProductJPARepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProductFacadeTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductJPARepository productJpaRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private LikeJpaRepository likeJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @BeforeEach
    void initializeEachTest() {
        Product product = Product.builder()
            .code("A0001")
            .brandCode("B0001")
            .name("테스트 상품1")
            .category1("ELECTRIC")
            .category2("ELECTRIC_BATTERY")
            .price(BigDecimal.valueOf(10000))
            .useYn(true)
            .quantity(10L)
            .build();

        productJpaRepository.save(product);

        Brand brand = Brand.builder()
            .code("B0001")
            .name("테스트 브랜드")
            .description("브랜드 설명")
            .useYn(true)
            .build();
        brandJpaRepository.save(brand);

        Like like = Like.builder()
            .productId("A0001")
            .userId("utlee")
            .build();
        likeJpaRepository.save(like);
    }

    @AfterEach
    void terminateEachTest() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("물품 정보 조회")
    class GetProduct {

        @DisplayName("성공")
        @Test
        void existsProduct(){
            //given
            String productCode = "A0001";

            //when
            ProductResult productResult = productFacade.getProduct(productCode);

            //then
            assertAll(
                () -> assertThat(productResult).isNotNull(),
                () -> assertThat(productResult.brandInfo().getCode()).isEqualTo("B0001"),
                () -> assertThat(productResult.name()).isNotNull()
            );

        }

        @DisplayName("없는 상품 조회 - 404에러")
        @Test
        void notExistsProduct(){
            //given
            String productCode = "A0002";

            //when
            CoreException result = Assert.assertThrows(CoreException.class, () -> {
                ProductResult productResult = productFacade.getProduct(productCode);
            }) ;

            //then
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);

        }
    }
}
