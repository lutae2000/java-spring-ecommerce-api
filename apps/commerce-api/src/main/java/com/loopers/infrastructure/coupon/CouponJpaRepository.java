package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.Coupon;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    /**
     * 회원에게 할당된 사용 가능 쿠폰 찾기
     * @param couponCode
     * @return
     */
    @Query("select c from Coupon c where c.userId = :userId and c.useYn = false")
    Optional<List<Coupon>> findCouponsByUserId(String userId);

    /**
     * 쿠폰코드 & 회원ID 조건으로 쿠폰이 있는지 조회
     * @param userId
     * @param code
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.userId = :userId and c.couponNo = :couponNo")
    Optional<Coupon> findCouponByCouponNoAndUserId(String userId, String couponNo);

    /**
     * 쿠폰코드 조건으로 쿠폰이 있는지 조회 조회
     * @param userId
     * @param code
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.couponNo = :couponNo")
    Optional<Coupon> findCouponByCouponNo(String couponNo);

    /**
     * 쿠폰 상태 변경
     * @param coupon
     * @return
     */
    @Query("update Coupon c set c.useYn = true where c.couponNo = :couponNo and c.userId = :userId")
    @Modifying
    void updateCouponUseYn(@Param("userId") String userId, @Param("couponNo") String couponNo);

}
