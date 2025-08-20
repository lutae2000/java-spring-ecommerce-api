package com.loopers.application.product;

import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.SortBy;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductFacade {
    private final ProductService productService;
    private final LikeService likeService;
    private final BrandService brandService;
    private final ProductRepository productRepository;

    /**
     * 물품 단건 조회
     * @param productId
     * @return
     */
    public ProductResult getProduct(String productId) {
        ProductInfo productInfo = productService.findProduct(productId);
        BrandInfo brandInfo = brandService.findByBrandCode(productInfo.getBrandCode());
        LikeSummary likeSummary = likeService.likeSummaryByProductId(productId);
        return ProductResult.of(productInfo, brandInfo, likeSummary.getLikesCount());
    }

    /**
     * 물품 생성
     * @param productCriteria
     */
    public void createProduct(ProductCriteria productCriteria){
        // 중복 코드 체크 - ProductRepository 직접 사용
        if (productRepository.findProduct(productCriteria.code()) != null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 상품 코드입니다");
        }
        
        ProductCommand productCommand = ProductCriteria.toCommand(productCriteria);
        productService.createProduct(productCommand);
    }

    /**
     * 물품 리스트 조회
     * @param productCriteria
     * @return
     */
    public ProductPageResult getProductList(ProductCriteria productCriteria){
        ProductPageResult productPageResult = productService.findProductListByBrandCode(
            productCriteria.brandCode(),
            productCriteria.sortBy(),
            ProductCriteria.toPageable(productCriteria)
        );

        List<ProductInfo> enrichedAndSortedProducts = enrichProductPageWithLikeSummary(
            productPageResult.getProducts(), 
            productCriteria.sortBy()
        );
        
        // 정렬된 리스트를 ProductPageResult에 설정
        productPageResult = ProductPageResult.builder()
            .products(enrichedAndSortedProducts)
            .page(productPageResult.getPage())
            .size(productPageResult.getSize())
            .totalElements(productPageResult.getTotalElements())
            .totalPages(productPageResult.getTotalPages())
            .hasNext(productPageResult.isHasNext())
            .hasPrevious(productPageResult.isHasPrevious())
            .isFirst(productPageResult.isFirst())
            .isLast(productPageResult.isLast())
            .build();
            
        return productPageResult;
    }

    /**
     * likeSummary와 조합
     * @param productInfos
     */
    private List<ProductInfo> enrichProductPageWithLikeSummary(List<ProductInfo> productInfos, SortBy sortBy) {
        try {
            log.debug("enrichProductPageWithLikeSummary 시작 - 상품 수: {}, 정렬: {}",
                productInfos != null ? productInfos.size() : 0, sortBy);

            // Null 체크
            if (productInfos == null || productInfos.isEmpty()) {
                log.debug("정렬할 상품이 없습니다");
                return new ArrayList<>(); // 빈 리스트 반환 (예외 대신)
            }

            List<String> productCodes = productInfos.stream()
                .map(ProductInfo::getCode)
                .filter(code -> code != null) // null 코드 필터링
                .toList();

            Map<String, Long> likeCountMap = likeService.findLikeSummaryByProductCodes(productCodes);

            // 각 ProductInfo에 좋아요 수 설정
            productInfos.forEach(productInfo -> {
                if (productInfo != null && productInfo.getCode() != null) {
                    Long likeCount = likeCountMap.getOrDefault(productInfo.getCode(), 0L);
                    productInfo.setLikeCount(likeCount);
                }
            });


            return productInfos;
        } catch (Exception e) {
            log.error("enrichProductPageWithLikeSummary 실패 - 정렬: {}", sortBy, e);
            throw e;
        }
    }

}
