package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.loopers.application.product.ProductPageResult;
import com.loopers.domain.brand.Brand;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.utils.RedisCacheTemplate;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @MockBean
    RedisCacheTemplate redisCacheTemplate;


    private Brand testBrand;
    private Product testProduct;

    @BeforeEach
    void setup(){
        // Brand 먼저 생성하고 저장
        testBrand = Brand.builder()
            .code("B0001")
            .name("테스트 브랜드")
            .description("테스트 브랜드 설명")
            .imgURL("brand.jpg")
            .useYn(true)
            .build();

        // Brand를 먼저 저장
        testBrand = brandJpaRepository.save(testBrand);

        // Product 생성 - 저장된 Brand 객체 사용
        testProduct = Product.createWithBrand(
            "A0001",
            "상품1",
            BigDecimal.valueOf(10000),
            10L,
            "https://naver.com/img",
            "상품에 대한 설명",
            testBrand.getCode(),
            "ELECTRIC",
            "컴퓨터",
            "노트북"
        );

        testProduct = productRepository.save(testProduct);

        // 추가 상품 생성 (브랜드별 조회 테스트용)
        Product product2 = Product.createWithBrand(
            "A0002",
            "상품2",
            BigDecimal.valueOf(20000),
            20L,
            "https://naver.com/img2",
            "상품2에 대한 설명",
            testBrand.getCode(),
            "ELECTRIC",
            "컴퓨터",
            "데스크톱"
        );
        productRepository.save(product2);
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
            ProductInfo expectedProductInfo = ProductInfo.from(testProduct);
            when(redisCacheTemplate.getOrSet(anyString(), any(), any(Duration.class), any()))
                .thenReturn(expectedProductInfo);

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
            //given
            when(redisCacheTemplate.getOrSet(anyString(), any(), any(Duration.class), any()))
                .thenThrow(new CoreException(ErrorType.NOT_FOUND, "검색하려는 물품이 없습니다"));

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
    @Disabled
    class CreateProduct {

        @Test
        @DisplayName("성공")
        void createProduct_success(){
            //given


            //when
//            productService.createProduct(command);

            //then
            Product savedProduct = productRepository.findProduct("A0003");
            assertThat(savedProduct).isNotNull();
            assertThat(savedProduct.getName()).isEqualTo("상품3");
            assertThat(savedProduct.getCode()).isEqualTo("A0003");
        }
    }

    @Nested
    @DisplayName("물품 상세조회")
    class GetProductDetailInfo {

        @Test
        @DisplayName("성공 - Redis 캐시 미스 후 DB 조회")
        void findProduct_success_with_cache_miss(){
            // given
            ProductInfo expectedProductInfo = ProductInfo.from(testProduct);
            when(redisCacheTemplate.getOrSet(anyString(), any(), any(Duration.class), any()))
                .thenAnswer(invocation -> {
                    // supplier 호출 (DB 조회 )
                    return expectedProductInfo;
                });

            // when
            ProductInfo result = productService.findProduct("A0001");

            // then
            assertAll(
                () -> assertThat(result.getCode()).isEqualTo("A0001"),
                () -> assertThat(result.getName()).isEqualTo("상품1"),
                () -> assertThat(result.getQuantity()).isEqualTo(10L),
                () -> assertThat(result.getBrandCode()).isEqualTo("B0001")
            );

            // Redis 호출 확인
            verify(redisCacheTemplate, times(1))
                .getOrSet(anyString(), eq(ProductInfo.class), any(Duration.class), any());
        }

        @Test
        @DisplayName("성공 - Redis 캐시 히트")
        void findProduct_success_with_cache_hit(){
            // given
            ProductInfo cachedProductInfo = ProductInfo.from(testProduct);
            when(redisCacheTemplate.getOrSet(anyString(), any(), any(Duration.class), any()))
                .thenReturn(cachedProductInfo);

            // when
            ProductInfo result = productService.findProduct("A0001");

            // then
            assertAll(
                () -> assertThat(result.getCode()).isEqualTo("A0001"),
                () -> assertThat(result.getName()).isEqualTo("상품1"),
                () -> assertThat(result.getQuantity()).isEqualTo(10L),
                () -> assertThat(result.getBrandCode()).isEqualTo("B0001")
            );

            // Redis 호출 확인
            verify(redisCacheTemplate, times(1))
                .getOrSet(anyString(), eq(ProductInfo.class), any(Duration.class), any());
        }

        @Test
        @DisplayName("실패 - 상품 미존재시 404에러")
        void findProduct_failure_product_not_found(){
            // given
            when(redisCacheTemplate.getOrSet(anyString(), any(), any(Duration.class), any()))
                .thenThrow(new CoreException(ErrorType.NOT_FOUND, "검색하려는 물품이 없습니다"));

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> {
                productService.findProduct("A0002");
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(exception.getMessage()).contains("검색하려는 물품이 없습니다");
        }

        @Test
        @DisplayName("실패 - Redis 연결 실패시 DB 조회로 fallback")
        void findProduct_failure_redis_fallback_to_db(){
            // given
            ProductInfo expectedProductInfo = ProductInfo.from(testProduct);
            
            // Redis get 메서드에서 예외 발생 
            when(redisCacheTemplate.get(anyString(), eq(ProductInfo.class)))
                .thenThrow(new RuntimeException("Redis 연결 실패"));
            
            when(redisCacheTemplate.getOrSet(anyString(), any(), any(Duration.class), any()))
                .thenAnswer(invocation -> {
                    // supplier 호출 (DB 조회 )
                    return expectedProductInfo;
                });

            // when
            ProductInfo result = productService.findProduct("A0001");

            // then
            assertAll(
                () -> assertThat(result.getCode()).isEqualTo("A0001"),
                () -> assertThat(result.getName()).isEqualTo("상품1")
            );

            // Redis 호출 확인 (실패 후 supplier 재호출)
            verify(redisCacheTemplate, times(1))
                .getOrSet(anyString(), eq(ProductInfo.class), any(Duration.class), any());
        }
    }

    @Nested
    @DisplayName("브랜드별 상품 리스트 조회")
    class GetProductListByBrandCode {

        @Test
        @DisplayName("성공 - Redis 캐시 미스 후 DB 조회")
        void findProductListByBrandCode_success_with_cache_miss(){
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = productRepository.findProductListByBrandCode("B0001", pageable);
            ProductPageResult expectedResult = ProductPageResult.from(productPage.map(ProductInfo::from));

            when(redisCacheTemplate.getOrSet(anyString(), any(), any(Duration.class), any()))
                .thenAnswer(invocation -> {
                    // supplier 호출 (DB 조회 )
                    return expectedResult;
                });

            // when
            ProductPageResult result = productService.findProductListByBrandCode("B0001", pageable);

            // then
            assertAll(
                () -> assertThat(result.getProducts()).hasSize(2),
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getPage()).isEqualTo(0),
                () -> assertThat(result.getSize()).isEqualTo(10),
                () -> assertThat(result.getProducts().get(0).getCode()).isEqualTo("A0001"),
                () -> assertThat(result.getProducts().get(1).getCode()).isEqualTo("A0002")
            );

            // Redis 호출 확인
            verify(redisCacheTemplate, times(1))
                .getOrSet(anyString(), eq(ProductPageResult.class), any(Duration.class), any());
        }

        @Test
        @DisplayName("성공 - Redis 캐시 히트")
        void findProductListByBrandCode_success_with_cache_hit(){
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = productRepository.findProductListByBrandCode("B0001", pageable);
            ProductPageResult cachedResult = ProductPageResult.from(productPage.map(ProductInfo::from));

            when(redisCacheTemplate.getOrSet(anyString(), any(), any(Duration.class), any()))
                .thenReturn(cachedResult);

            // when
            ProductPageResult result = productService.findProductListByBrandCode("B0001", pageable);

            // then
            assertAll(
                () -> assertThat(result.getProducts()).hasSize(2),
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getPage()).isEqualTo(0),
                () -> assertThat(result.getSize()).isEqualTo(10)
            );

            // Redis 호출 확인
            verify(redisCacheTemplate, times(1))
                .getOrSet(anyString(), eq(ProductPageResult.class), any(Duration.class), any());
        }

        @Test
        @DisplayName("성공 - 페이징 처리")
        void findProductListByBrandCode_success_with_paging(){
            // given
            Pageable pageable = PageRequest.of(0, 1); // 페이지 크기 1
            Page<Product> productPage = productRepository.findProductListByBrandCode("B0001", pageable);
            ProductPageResult expectedResult = ProductPageResult.from(productPage.map(ProductInfo::from));

            when(redisCacheTemplate.getOrSet(anyString(), any(), any(Duration.class), any()))
                .thenReturn(expectedResult);

            // when
            ProductPageResult result = productService.findProductListByBrandCode("B0001", pageable);

            // then
            assertAll(
                () -> assertThat(result.getProducts()).hasSize(1),
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.getPage()).isEqualTo(0),
                () -> assertThat(result.getSize()).isEqualTo(1),
                () -> assertThat(result.isHasNext()).isTrue(),
                () -> assertThat(result.isHasPrevious()).isFalse()
            );
        }

        @Test
        @DisplayName("성공 - 빈 결과")
        void findProductListByBrandCode_success_empty_result(){
            // given
            Pageable pageable = PageRequest.of(0, 10);
            when(redisCacheTemplate.getOrSet(anyString(), any(), any(Duration.class), any()))
                .thenAnswer(invocation -> {
                    // 빈 결과 반환
                    return ProductPageResult.builder()
                        .products(List.of())
                        .page(0)
                        .size(10)
                        .totalElements(0)
                        .totalPages(0)
                        .hasNext(false)
                        .hasPrevious(false)
                        .isFirst(true)
                        .isLast(true)
                        .build();
                });

            // when
            ProductPageResult result = productService.findProductListByBrandCode("NON_EXISTENT_BRAND", pageable);

            // then
            assertAll(
                () -> assertThat(result.getProducts()).isEmpty(),
                () -> assertThat(result.getTotalElements()).isEqualTo(0),
                () -> assertThat(result.getTotalPages()).isEqualTo(0),
                () -> assertThat(result.isHasNext()).isFalse(),
                () -> assertThat(result.isHasPrevious()).isFalse()
            );
        }

        @Test
        @DisplayName("실패 - Redis 연결 실패시 DB 조회로 fallback")
        void findProductListByBrandCode_failure_redis_fallback_to_db(){
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = productRepository.findProductListByBrandCode("B0001", pageable);
            ProductPageResult expectedResult = ProductPageResult.from(productPage.map(ProductInfo::from));

            // Redis get 메서드에서 예외 발생 
            when(redisCacheTemplate.get(anyString(), eq(ProductPageResult.class)))
                .thenThrow(new RuntimeException("Redis 연결 실패"));
            
            when(redisCacheTemplate.getOrSet(anyString(), any(), any(Duration.class), any()))
                .thenAnswer(invocation -> {
                    // supplier 호출 (DB 조회 )
                    return expectedResult;
                });

            // when
            ProductPageResult result = productService.findProductListByBrandCode("B0001", pageable);

            // then
            assertAll(
                () -> assertThat(result.getProducts()).hasSize(2),
                () -> assertThat(result.getTotalElements()).isEqualTo(2)
            );

            // Redis 호출 확인 (실패 후 supplier 재호출)
            verify(redisCacheTemplate, times(1))
                .getOrSet(anyString(), eq(ProductPageResult.class), any(Duration.class), any());
        }
    }
}
