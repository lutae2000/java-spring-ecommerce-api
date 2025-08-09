package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponCommand;

public record CouponCriteria (
    String userId,
    String couponNo
){
    public CouponCommand toCommand(String userId, String couponNo) {
        return new CouponCommand(userId, couponNo);
    }
}
