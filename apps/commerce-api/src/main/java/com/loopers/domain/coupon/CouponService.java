package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.micrometer.common.util.StringUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    /**
     * 회원에게 할당된 쿠폰들 찾기
     * @param couponCode
     * @return
     */
    @Transactional(readOnly = true)
    public List<Coupon> getCouponsByUserId(String userId){

        if(StringUtils.isEmpty(userId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "회원ID가 없습니다");
        }

        List<Coupon> couponList = couponRepository.findCouponsByUserId(userId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "조회된 쿠폰이 없습니다"));

        return couponList;
    }

    /**
     * 회원번호와 쿠폰번호로 쿠폰 조회
     */
    @Transactional(readOnly = true)
    public Coupon getCouponByUserIdAndCouponCode(CouponCommand couponCommand){
        Coupon coupon = couponRepository.findCouponsByCouponNoAndUserId(couponCommand.userId(), couponCommand.couponNo())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND,"유효한 쿠폰이 없습니다"));

        if(Boolean.TRUE.equals(coupon.getUseYn())){
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다");
        }
        return coupon;
    }

    /**
     * 쿠폰번호로 조회
     * @param couponCode
     * @return
     */
    @Transactional(readOnly = true)
    public Coupon getCouponByCouponNo(String couponCode){

        if(StringUtils.isEmpty(couponCode)){
            throw new CoreException(ErrorType.BAD_REQUEST, "입력된 쿠폰번호가 없습니다");
        }
        Coupon coupon = couponRepository.findCouponByCouponNo(couponCode)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "유효한 쿠폰이 없습니다"));

        if(Boolean.TRUE.equals(coupon.getUseYn())){
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다");
        }
        return coupon;
    }


    /**
     * 쿠폰 사용 상태 변경
     * @param coupon
     * @return
     */
    @Transactional
    public void updateCouponUseYn(CouponCommand couponCommand){
        Coupon coupon = this.getCouponByCouponNo(couponCommand.couponNo());

        if(ObjectUtils.isNotEmpty(coupon)){
            couponRepository.updateCouponUseYn(couponCommand);
        }
    }

    /**
     * 쿠폰 저장
     * @param coupon
     * @return
     */
    @Transactional
    public Coupon save(Coupon coupon){
        return couponRepository.save(coupon);
    }
}
