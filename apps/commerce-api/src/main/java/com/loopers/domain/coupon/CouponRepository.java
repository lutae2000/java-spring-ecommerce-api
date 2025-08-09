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
     * 쿠폰코드 & 회원ID 조건으로 쿠폰이 있는지 조회 조회
     * @param userId
     * @param couponNo
     * @return
     */
    Optional<Coupon> findCouponsByCouponNoAndUserId(String userId, String couponNo);

    /**
     * 쿠폰코드로 존재하는 쿠폰이 있는지 조회
     */
    Optional<Coupon> findCouponByCouponNo(String couponNo);

    /**
     * 쿠폰 상태 변경
     * @param coupon
     * @return
     */
    void updateCouponUseYn(CouponCommand coupon);

    /**
     * 쿠폰 상태 변경
     * @param coupon
     * @return
     */
    Coupon save(Coupon coupon);
}
