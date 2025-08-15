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
import com.loopers.domain.product.SortBy;
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
    private LikeService likeService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @BeforeEach
    void initializeEachTest() {
        Product product = Product.builder()
            .code("A0001")
            .brand("B0001")
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

            //when & then
            Exception exception = Assert.assertThrows(Exception.class, () -> {
                ProductResult productResult = productFacade.getProduct(productCode);
            });

            // CoreException이 RuntimeException으로 래핑되어 있을 수 있으므로 원인 확인
            final Throwable cause = getRootCause(exception);

            assertAll(
                () -> assertThat(cause).isInstanceOf(CoreException.class),
                () -> assertThat(((CoreException) cause).getErrorType()).isEqualTo(ErrorType.NOT_FOUND),
                () -> assertThat(((CoreException) cause).getMessage()).isEqualTo("검색하려는 물품이 없습니다")
            );
        }
    }

    @Nested
    @DisplayName("물품 리스트 조회")
    class GetProductList {

        @DisplayName("성공 - 브랜드별 조회")
        @Test
        void getProductListByBrandCode() {
            // given
            ProductCriteria criteria = ProductCriteria.builder()
                .brandCode("B0001")
                .page(0)
                .size(10)
                .sortBy(SortBy.LIKE_DESC)
                .build();

            // when
            ProductPageResult result = productFacade.getProductList(criteria);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getProducts()).isNotEmpty(),
                () -> assertThat(result.getProducts().get(0).getCode()).isEqualTo("A0001"),
                () -> assertThat(result.getProducts().get(0).getBrandCode()).isEqualTo("B0001")
            );
        }

        @DisplayName("성공 - 빈 브랜드 조회")
        @Test
        void getProductListByEmptyBrandCode() {
            // given
            ProductCriteria criteria = ProductCriteria.builder()
                .brandCode("NON_EXISTENT")
                .page(0)
                .size(10)
                .sortBy(SortBy.LIKE_DESC)
                .build();

            // when
            ProductPageResult result = productFacade.getProductList(criteria);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getProducts()).isEmpty()
            );
        }

        @DisplayName("성공 - 정렬 테스트 (좋아요 내림차순)")
        @Test
        void getProductListWithLikeDescSort() {
            // given - 추가 상품 생성
            Product product2 = Product.builder()
                .code("A0002")
                .brand("B0001")
                .name("테스트 상품2")
                .category1("ELECTRIC")
                .category2("ELECTRIC_BATTERY")
                .price(BigDecimal.valueOf(20000))
                .useYn(true)
                .quantity(5L)
                .build();
            productJpaRepository.save(product2);

            // LikeService를 사용하여 좋아요 생성 (LikeSummary 자동 업데이트)
            likeService.like("user1", "A0002");
            likeService.like("user2", "A0002");
            
            // A0001 상품에 대한 좋아요도 생성 (기본 상품)
            likeService.like("user3", "A0001");

            ProductCriteria criteria = ProductCriteria.builder()
                .brandCode("B0001")
                .page(0)
                .size(10)
                .sortBy(SortBy.LIKE_DESC)
                .build();

            // when
            ProductPageResult result = productFacade.getProductList(criteria);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getProducts()).hasSize(2),
                () -> assertThat(result.getProducts().get(0).getCode()).isEqualTo("A0002"), // 좋아요 2개 (내림차순)
                () -> assertThat(result.getProducts().get(0).getLikeCount()).isEqualTo(2L),
                () -> assertThat(result.getProducts().get(1).getCode()).isEqualTo("A0001"), // 좋아요 1개
                () -> assertThat(result.getProducts().get(1).getLikeCount()).isEqualTo(1L)
            );
        }

        @DisplayName("성공 - 가격 정렬 테스트")
        @Test
        void getProductListWithPriceSort() {
            // given - 추가 상품 생성
            Product product2 = Product.builder()
                .code("A0002")
                .brand("B0001")
                .name("테스트 상품2")
                .category1("ELECTRIC")
                .category2("ELECTRIC_BATTERY")
                .price(BigDecimal.valueOf(5000)) // 더 저렴한 가격
                .useYn(true)
                .quantity(5L)
                .build();
            productJpaRepository.save(product2);

            ProductCriteria criteria = ProductCriteria.builder()
                .brandCode("B0001")
                .page(0)
                .size(10)
                .sortBy(SortBy.PRICE_ASC)
                .build();

            // when
            ProductPageResult result = productFacade.getProductList(criteria);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getProducts()).hasSize(2),
                () -> assertThat(result.getProducts().get(0).getCode()).isEqualTo("A0002"), // 가격 5000 (오름차순)
                () -> assertThat(result.getProducts().get(0).getPrice().compareTo(BigDecimal.valueOf(5000))).isEqualTo(0),
                () -> assertThat(result.getProducts().get(1).getCode()).isEqualTo("A0001"), // 가격 10000
                () -> assertThat(result.getProducts().get(1).getPrice().compareTo(BigDecimal.valueOf(10000))).isEqualTo(0)
            );
        }
    }

    @Nested
    @DisplayName("물품 생성")
    class CreateProduct {

        @DisplayName("성공 - 물품 생성")
        @Test
        void createProductSuccess() {
            // given
            ProductCriteria criteria = ProductCriteria.builder()
                .code("A0003")
                .name("새로운 상품")
                .price(BigDecimal.valueOf(15000))
                .quantity(20L)
                .brandCode("B0001")
                .category1("ELECTRIC")
                .category2("ELECTRIC_BATTERY")
                .category3("BATTERY")
                .description("새로운 상품 설명")
                .imgURL("http://example.com/image.jpg")
                .build();

            // when & then
            try {
                productFacade.createProduct(criteria);
            } catch (Exception e) {
                Assert.fail("상품 생성이 실패했습니다: " + e.getMessage());
            }

            // 생성된 상품 확인
            Product createdProduct = productJpaRepository.findByCode("A0003");
            assertAll(
                () -> assertThat(createdProduct).isNotNull(),
                () -> assertThat(createdProduct.getName()).isEqualTo("새로운 상품"),
                () -> assertThat(createdProduct.getPrice().compareTo(BigDecimal.valueOf(15000))).isEqualTo(0),
                () -> assertThat(createdProduct.getQuantity()).isEqualTo(20L)
            );
        }

        @DisplayName("실패 - 중복 코드로 생성")
        @Test
        void createProductWithDuplicateCode() {
            // given
            ProductCriteria criteria = ProductCriteria.builder()
                .code("A0001") // 이미 존재하는 코드
                .name("중복 상품")
                .price(BigDecimal.valueOf(15000))
                .quantity(20L)
                .brandCode("B0001")
                .category1("ELECTRIC")
                .category2("ELECTRIC_BATTERY")
                .category3("BATTERY")
                .description("중복 상품 설명")
                .imgURL("http://example.com/image.jpg")
                .build();

            // when & then
            Exception exception = Assert.assertThrows(Exception.class, () -> {
                productFacade.createProduct(criteria);
            });

            // 예외 원인 확인
            final Throwable cause = getRootCause(exception);

            assertAll(
                () -> assertThat(cause).isInstanceOf(CoreException.class),
                () -> assertThat(((CoreException) cause).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST)
            );
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
