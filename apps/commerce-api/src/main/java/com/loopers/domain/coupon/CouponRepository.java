package com.loopers.domain.coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {

    /**
     * 회원에게 할당된 쿠폰 찾기
     *
     * @param couponCode
     * @return
     */
    Optional<List<Coupon>> findCouponsByUserId(String userId);

    /**
     * 쿠폰 상태 변경
     * @param coupon
     * @return
     */
    Coupon updateCouponUseYn(CouponCommand coupon);

    /**
     * 쿠폰 상태 변경
     * @param coupon
     * @return
     */
    Coupon save(Coupon coupon);
}
