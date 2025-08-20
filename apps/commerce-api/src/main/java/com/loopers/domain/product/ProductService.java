package com.loopers.domain.product;

import com.loopers.application.product.ProductPageResult;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * product 생성(upsert)
     */
    @Transactional
    public void createProduct(ProductCommand productCommand){
        Product product = ProductCommand.toProduct(productCommand);
        productRepository.save(product);

        // 캐시 무효화
        productRepository.invalidateProductCache(product.getCode());
    }

    /**
     * 물품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductInfo findProduct(String productId){
        Product product = productRepository.findProductWithCache(productId, Duration.ofMinutes(1));

        if(ObjectUtils.isEmpty(product)){
            throw new CoreException(ErrorType.NOT_FOUND, "검색하려는 물품이 없습니다");
        }

        return ProductInfo.from(product);
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
    public ProductPageResult findProductListByBrandCode(String brandCode, SortBy sortBy, Pageable pageable) {
        return productRepository.findProductListByBrandCodeWithCache(
            brandCode, sortBy, pageable, Duration.ofMinutes(1)
        );
    }
}
