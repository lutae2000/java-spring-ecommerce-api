package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponCommand;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    /**
     * 회원에게 할당된 쿠폰 찾기
     * @param couponCode
     * @return
     */
    @Query("select c from Coupon c where c.userId = :userId and c.useYn = 'N'")
    Optional<List<Coupon>> findCouponsByUserId(String userId);

    /**
     * 쿠폰 상태 변경
     * @param coupon
     * @return
     */
    @Modifying
    @Query("update Coupon c set c.useYn = 'Y' where c.couponNo = :couponNo and c.userId = :userId")
    Coupon updateCouponUseYn(CouponCommand coupon);

    /**
     * 쿠폰 저장
     * @param coupon
     * @return
     */
    Coupon save(Coupon coupon);
}
