package com.loopers.infrastructure.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.application.product.ProductPageResult;
import com.loopers.config.redis.RedisCacheTemplate;
import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.SortBy;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJPARepository productJPARepository;
    private final RedisCacheTemplate redisCacheTemplate;

    @Override
    public Product findProductForUpdate(String productId) {
        return productJPARepository.findProductByCodeForUpdate(productId);
    }

    @Override
    public Product findProduct(String productId) {
        return productJPARepository.findByCode(productId);
    }

    /**
     * 캐시에서 Product 조회 (캐시 미스 시 supplier 호출)
     */
    @Override
    public Product findProductWithCache(String productId, Duration ttl) {
        String cacheKey = RedisCacheTemplate.generateKey("product", productId);
        log.debug("findProductWithCache - key: {}, ttl: {}", cacheKey, ttl);

        return redisCacheTemplate.getOrSet(cacheKey, Product.class, ttl, () -> {
            log.debug("Cache miss - DB에서 조회: {}", productId);
            return productJPARepository.findByCode(productId);
        });
    }

    @Override
    public ProductPageResult findProductListByBrandCodeWithCache(String brandCode, SortBy sortBy, Pageable pageable, Duration ttl) {
        // sortBy가 null인 경우 기본값 사용
        SortBy actualSortBy = sortBy != null ? sortBy : SortBy.LIKE_DESC;

        String cacheKey = RedisCacheTemplate.generateKey(
            "product", "brand", brandCode, pageable.getPageNumber(), pageable.getPageSize(), actualSortBy.name()
        );
        log.debug("findProductListByBrandCodeWithCache - key: {}, ttl: {}", cacheKey, ttl);

        return redisCacheTemplate.getOrSet(cacheKey, ProductPageResult.class, ttl, () -> {
            log.debug("Cache miss - DB에서 조회: brand={}, page={}, size={}, sort={}",
                brandCode, pageable.getPageNumber(), pageable.getPageSize(), actualSortBy);

            // LikeSummary와 조인하여 정렬된 결과 조회
            Page<Object[]> resultPage = productJPARepository.findProductListByBrandCode(
                brandCode, actualSortBy.name(), pageable
            );

            // Object[]를 ProductInfo로 변환 (Product + likesCount)
            List<ProductInfo> productInfos = resultPage.getContent().stream()
                .map(row -> {
                    Product product = (Product) row[0];
                    Long likesCount = (Long) row[1];

                    ProductInfo productInfo = ProductInfo.from(product);
                    productInfo.setLikeCount(likesCount);
                    return productInfo;
                })
                .toList();


            // ProductPageResult 생성
            return ProductPageResult.builder()
                .products(productInfos)
                .page(resultPage.getNumber())
                .size(resultPage.getSize())
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .hasNext(resultPage.hasNext())
                .hasPrevious(resultPage.hasPrevious())
                .isFirst(resultPage.isFirst())
                .isLast(resultPage.isLast())
                .build();
        });
    }


    @Override
    public Page<Object[]> findProductListByBrandCode(String brandCode, SortBy sortBy, Pageable pageable) {
        // sortBy가 null인 경우 기본값 사용
        SortBy actualSortBy = sortBy != null ? sortBy : SortBy.LIKE_DESC;

        return productJPARepository.findProductListByBrandCode(brandCode, actualSortBy.name(), pageable);
    }

    @Override
    public Product save(Product product) {
        Product savedProduct = productJPARepository.save(product);
        return savedProduct;
    }

    @Override
    public void updateProduct(String productId, Long quantity, OrderStatus orderStatus) {

        Product product = productJPARepository.findProductByCodeForUpdate(productId);

        //주문시
        if(orderStatus.equals(OrderStatus.ORDER_PLACED) && product.getQuantity() > quantity){
            product.setQuantity(product.getQuantity() - quantity);
        } else if(orderStatus.equals(OrderStatus.ORDER_CANCEL)){
            product.setQuantity(product.getQuantity() + quantity);
        }

        productJPARepository.save(product);
    }


    @Override
    public void saveAll(List<Product> products) {
        productJPARepository.saveAll(products);
    }

    @Override
    public int count() {
        int count = productJPARepository.countAllByCode("p1");
        return count;
    }

    @Override
    public void invalidateProductCache(String productId) {
        // 개별 상품 캐시 삭제
        String detailKey = RedisCacheTemplate.generateKey("product", productId);
        redisCacheTemplate.delete(detailKey);

        // 관련 리스트 캐시 삭제
        redisCacheTemplate.deleteByPattern("product:brand:*");

        log.info("Invalidated product cache for productId: {}", productId);
    }
}
