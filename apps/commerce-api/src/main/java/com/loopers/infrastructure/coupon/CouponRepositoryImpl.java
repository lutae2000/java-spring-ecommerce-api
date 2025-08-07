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
     * 쿠폰 상태 변경
     * @param coupon
     * @return
     */
    @Override
    public Coupon updateCouponUseYn(CouponCommand coupon) {
        return couponJpaRepository.updateCouponUseYn(coupon);
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
