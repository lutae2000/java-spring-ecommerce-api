package com.loopers.domain.coupon;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CouponInfo {
    private String eventId;
    private String couponNo;
    private String userId;
    private String couponName;
    private Boolean useYn;
    private int discountRate;
    private BigDecimal discountRateLimitPrice;
    private BigDecimal discountAmountLimitPrice;
    private BigDecimal discountAmount;
}
