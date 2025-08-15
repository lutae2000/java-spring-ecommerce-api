package com.loopers.domain.coupon;

import com.loopers.application.coupon.CouponCriteria;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCommand;

public record CouponCommand (
    String userId,
    String couponNo
){
    public static CouponCriteria of(String userId, String couponNo) {
        return new CouponCriteria(userId, couponNo);
    }

    public static Product toProduct(ProductCommand productCommand){
        return Product.from(productCommand);
    }

    // Brand 객체가 필요한 경우를 위한 메서드 추가
    public static Product toProductWithBrand(ProductCommand productCommand, Brand brand){
        return Product.from(productCommand, brand);
    }
}
