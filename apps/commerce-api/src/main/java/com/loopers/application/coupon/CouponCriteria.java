package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponCommand;

public record CouponCriteria (
    String userId,
    String couponNo
){
    public static CouponCriteria of(String userId, String couponNo) {
        return new CouponCommand(String userId, String couponNo);
    }
}
