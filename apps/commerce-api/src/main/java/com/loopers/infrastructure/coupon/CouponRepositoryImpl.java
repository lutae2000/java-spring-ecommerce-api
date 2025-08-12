package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.CouponRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CouponRepositoryImpl implements CouponRepository {
    private final CouponJpaRepository couponJpaRepository;

    /**
     * 회원에게 할당된 쿠폰 찾기
     * @param couponCode
     * @return
     */
    @Override
    public Optional<List<Coupon>> findCouponsByUserId(String userId) {
        return couponJpaRepository.findCouponsByUserId(userId);
    }

    /**
     * 쿠폰코드 & 회원ID 조건으로 쿠폰이 있는지 조회 조회
     * @param userId
     * @param couponNo
     * @return
     */
    @Override
    public Optional<Coupon> findCouponsByCouponNoAndUserId(String userId, String couponNo) {
        return couponJpaRepository.findCouponByCouponNoAndUserId(userId, couponNo);
    }

    /**
     * 쿠폰코드로 존재하는 쿠폰이 있는지 조회
     */
    @Override
    public Optional<Coupon> findCouponByCouponNo(String couponNo) {
        return couponJpaRepository.findCouponByCouponNo(couponNo);
    }

    /**
     * 쿠폰 상태 변경
     * @param coupon
     * @return
     */
    @Override
    public int updateCouponUseYn(CouponCommand coupon) {
        return couponJpaRepository.updateCouponUseYn(coupon.userId(), coupon.couponNo());
    }

    /**
     * 쿠폰 저장
     * @param coupon
     * @return
     */
    @Override
    public Coupon save(Coupon coupon) {
        return couponJpaRepository.save(coupon);
    }
}
