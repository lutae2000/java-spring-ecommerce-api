package com.loopers.domain.product;

import com.loopers.application.product.ProductPageResult;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.utils.RedisCacheTemplate;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisCacheTemplate redisCacheTemplate;

    /**
     * product 생성(upsert)
     */
    @Transactional
    public void createProduct(ProductCommand productCommand){
        Product product = ProductCommand.toProduct(productCommand);
        productRepository.save(product);
    }

    /**
     * 물품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductInfo findProduct(String productId){
        String cacheKey = RedisCacheTemplate.generateKey("product", productId);
        //테스트를 위해 Redis에 TTL 1분

        return redisCacheTemplate.getOrSet(cacheKey, ProductInfo.class, Duration.ofMinutes(1), () -> {
            log.debug("Cache miss - key: {}, DB에서 조회", cacheKey);
            Product product = productRepository.findProduct(productId);

            if(ObjectUtils.isEmpty(product)){
                throw new CoreException(ErrorType.NOT_FOUND, "검색하려는 물품이 없습니다");
            }

            return ProductInfo.from(product);
        });
    }


    /**
     * 주문 상품 처리
     * @param productId
     * @param quantity
     */
    @Transactional
    public void orderedStock(String productId, Long quantity){
        ProductInfo productInfo = findProduct(productId);

        if(ObjectUtils.isEmpty(productInfo)){
            throw new CoreException(ErrorType.NOT_FOUND, "주문하려는 물품코드가 없습니다");
        }
        if(productInfo.getQuantity() < quantity){
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다");
        }
        productRepository.orderProduct(productId, quantity);
    }

    /**
     * 브랜드로 상품 리스트 조회
     */
    @Transactional(readOnly = true)
    public ProductPageResult findProductListByBrandCode(String brandCode, Pageable pageable) {
        String cacheKey = RedisCacheTemplate.generateKey("product", "brand", brandCode, pageable.getPageNumber(), pageable.getPageSize());

        log.info("=== findProductListByBrandCode START ===");
        log.info("brandCode: {}, page: {}, size: {}", brandCode, pageable.getPageNumber(), pageable.getPageSize());
        log.info("Generated cache key: {}", cacheKey);


        //테스트를 위해 Redis에 TTL 1분
        ProductPageResult result = redisCacheTemplate.getOrSet(cacheKey, ProductPageResult.class, Duration.ofMinutes(1), () -> {
            log.info("Cache miss - DB에서 조회 시작");
            Page<ProductInfo> productPage = productRepository.findProductListByBrandCode(brandCode, pageable)
                .map(ProductInfo::from);

            ProductPageResult dbResult = ProductPageResult.from(productPage);
            log.info("DB 조회 완료 - products count: {}, totalElements: {}",
                dbResult.getProducts().size(), dbResult.getTotalElements());

            return dbResult;
        });
        log.info("=== findProductListByBrandCode END ===");
        return result;
    }


    /**
     * 캐시 무효화
     */
    public void invalidateProductCache(String productId) {
        // 개별 상품 캐시 삭제
        String detailKey = RedisCacheTemplate.generateKey("product", productId);
        redisCacheTemplate.delete(detailKey);

        // 관련 리스트 캐시 삭제
        redisCacheTemplate.deleteByPattern("product:*");
//        redisCacheTemplate.deleteByPattern("product:brand:*");

        log.info("Invalidated product cache for productId: {}", productId);
    }
}
