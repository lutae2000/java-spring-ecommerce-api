package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CouponFacade {
    private final CouponService couponService;

    /**
     * 회원에게 할당된 쿠폰들 찾기
     * @param couponCode
     * @return
     */
    public List<Coupon> getCouponsByUserId(String couponCode) {
        return couponService.getCouponsByUserId(couponCode);
    }

    /**
     * 회원번호와 쿠폰번호로 쿠폰 조회
     */
    public Coupon getCouponByUserIdAndCouponCode(CouponCriteria couponCriteria){
        return couponService.getCouponByUserIdAndCouponCode(couponCriteria.toCommand(couponCriteria.userId(), couponCriteria.couponNo()));
    }

    /**
     * 쿠폰번호로 조회
     * @param couponCode
     * @return
     */
    public Coupon getCouponByCouponNo(String couponCode){
        return couponService.getCouponByCouponNo(couponCode);
    }

    /**
     * 쿠폰 사용 상태 변경
     * @param coupon
     * @return
     */
    public void updateCouponUseYn(CouponCriteria couponCriteria){
        couponService.updateCouponUseYn(couponCriteria.toCommand(couponCriteria.userId(), couponCriteria.couponNo()));
    }
}
