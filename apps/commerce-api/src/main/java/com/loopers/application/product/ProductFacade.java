package com.loopers.application.product;

import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeInfo;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductFacade {
    private final ProductService productService;
    private final LikeService likeService;
    private final BrandService brandService;

    public ProductResult getProduct(String productId) {

        ProductInfo productInfo = productService.findProduct(productId);
        Long likeCount = likeService.countLike(productInfo.getCode());
        BrandInfo brandInfo = brandService.findByBrandCode(productInfo.getBrandCode());
        return ProductResult.of(productInfo, brandInfo, likeCount);
    }
}
