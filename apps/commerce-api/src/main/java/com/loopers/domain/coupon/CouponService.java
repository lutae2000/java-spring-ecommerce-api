package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void updateCouponUseYn(CouponCommand couponCommand){
        Optional<List<Coupon>> couponList = couponRepository.findCouponsByUserId(couponCommand.getUserId());
        if(couponList.isPresent()){
            List<Coupon> couponsResult = couponList.get();

            if(couponsResult.stream().anyMatch(coupons -> coupons.getCouponNo().equals(couponCommand.getCouponNo()))){
                couponRepository.updateCouponUseYn(couponCommand);
            }else {
                throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰이거나 사용할 수 없는 쿠폰입니다");
            }
        }
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
