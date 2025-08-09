package com.loopers.domain.coupon;

public record CouponCommand (
    String userId,
    String couponNo
){ }
