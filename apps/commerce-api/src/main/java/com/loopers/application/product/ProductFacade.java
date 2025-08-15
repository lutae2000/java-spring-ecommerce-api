package com.loopers.application.product;

import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.SortBy;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductFacade {
    private final ProductService productService;
    private final LikeService likeService;
    private final BrandService brandService;

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
     * @param criteria
     */
    public void createProduct(ProductCriteria criteria){
        ProductCommand productCommand = ProductCriteria.toCommand(criteria);
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
            ProductCriteria.toPageable(productCriteria)
        );

        enrichProductPageWithLikeSummary(productPageResult.getProducts(), productCriteria.sortBy());
        return productPageResult;
    }

    /**
     * likeSummary와 조합
     * @param productInfos
     */
    private void enrichProductPageWithLikeSummary(List<ProductInfo> productInfos, SortBy sortBy) {

        List<String> productCodes = productInfos.stream()
            .map(ProductInfo::getCode)
            .toList();

        Map<String, Long> likeCountMap = likeService.findLikeSummaryByProductCodes(productCodes);

        // 각 ProductInfo에 좋아요 수 설정
        productInfos.forEach(productInfo -> {
            Long likeCount = likeCountMap.getOrDefault(productInfo.getCode(), 0L);
            productInfo.setLikeCount(likeCount);
        });

        // 정렬 적용
        sortProductInfos(productInfos, sortBy);
    }

    /**
     * ProductInfo 리스트를 정렬
     * @param productInfos
     * @param sortBy
     */
    private void sortProductInfos(List<ProductInfo> productInfos, SortBy sortBy) {
        switch (sortBy) {
            case LIKE_DESC:
                productInfos.sort(Comparator.comparing(ProductInfo::getLikeCount, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case LIKE_ASC:
                productInfos.sort(Comparator.comparing(ProductInfo::getLikeCount, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case PRICE_ASC:
                productInfos.sort(Comparator.comparing(ProductInfo::getPrice, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case PRICE_DESC:
                productInfos.sort(Comparator.comparing(ProductInfo::getPrice, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case LATEST:
                // 최신순 정렬 (생성일 기준, ProductInfo에 createdAt 필드가 있다면)
                // productInfos.sort(Comparator.comparing(ProductInfo::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
                // 현재는 기본 순서 유지
                break;
            default:
                // 기본값은 좋아요 내림차순
                productInfos.sort(Comparator.comparing(ProductInfo::getLikeCount, Comparator.nullsLast(Comparator.reverseOrder())));
                break;
        }
    }
}
