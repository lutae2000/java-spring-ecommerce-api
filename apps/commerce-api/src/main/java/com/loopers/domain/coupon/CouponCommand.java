package com.loopers.domain.coupon;

import com.loopers.application.coupon.CouponCriteria;

public record CouponCommand (
    String userId,
    String couponNo
){
    public static CouponCriteria of(String userId, String couponNo) {
        return new CouponCriteria(userId, couponNo);
    }
}
