package com.loopers.application.product;

import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.product.ProductInfo;
import java.math.BigDecimal;



public record ProductResult (
    String code,
    String name,
    BigDecimal price,
    Long quantity,
    String imgURL,
    String description,
    String brandCode,
    String category1,
    String category2,
    String category3,
    BrandInfo brandInfo,
    Long likes
){
    public static ProductResult of(ProductInfo productInfo, BrandInfo brandInfo, Long likes){
        return new ProductResult(
            productInfo.getCode(),
            productInfo.getName(),
            productInfo.getPrice(),
            productInfo.getQuantity(),
            productInfo.getImgURL(),
            productInfo.getDescription(),
            productInfo.getBrandCode(),
            productInfo.getCategory1(),
            productInfo.getCategory2(),
            productInfo.getCategory3(),
            brandInfo,
            likes
        );
    }

    public static ProductResult of(ProductInfo productInfo){
        return of(productInfo, null, productInfo.getLikeCount());
    }
}
