package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    /**
     * 회원에게 할당된 쿠폰 찾기
     * @param couponCode
     * @return
     */
    public Optional<List<Coupon>> getCoupons(String userId){
        return couponRepository.findCouponsByUserId(userId);
    }

    /**
     * 쿠폰 사용 상태 변경
     * @param coupon
     * @return
     */
    public Coupon updateCouponUseYn(CouponCommand coupon){
        Optional<List<Coupon>> couponList = couponRepository.findCouponsByUserId(coupon.getUserId());
        if(couponList.isPresent()){
            if(couponList.stream().allMatch(coupons -> coupons.contains(coupon.getCouponCode()))){
                return couponRepository.updateCouponUseYn(coupon.getCouponCode());
            }
        }
        throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰이거나 사용할 수 없는 쿠폰입니다");
    }

    /**
     * 쿠폰 저장
     * @param coupon
     * @return
     */
    public Coupon save(Coupon coupon){
        return couponRepository.save(coupon);
    }
}
