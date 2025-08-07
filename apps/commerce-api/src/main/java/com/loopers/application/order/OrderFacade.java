package com.loopers.application.order;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderDetailCommand.orderItem;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointInfo;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderFacade {
    private final OrderService orderService;
    private final UserService userService;
    private final PointService pointService;
    private final ProductService productService;
    private final CouponService couponService;

    @Transactional
    public OrderResult orderSubmit(String userId, String couponNo, BigDecimal totalAmount, List<orderItem> orderItems){
        BigDecimal discountPrice = BigDecimal.ZERO;

        //회원 유효성 검증
        UserInfo userInfo = userService.getUserInfo(userId);
        if(ObjectUtils.isEmpty(userInfo)){
            throw new CoreException(ErrorType.BAD_REQUEST, "유효한 계정이 아닙니다");
        }

        //쿠폰 적용가능 여부 조회
        if(StringUtils.isNotEmpty(couponNo)){
            Optional<List<Coupon>> couponList = couponService.getCoupons(userId);

            log.debug("::: couponNoList ::: {} : ", couponList.isPresent());

            // F/E에서 가져온 쿠폰 번호 검증
            Optional<List<Coupon>> checkedCoupon = couponList.stream()
                        .filter(coupon -> coupon.contains(couponNo))
                        .findFirst();

            CouponCommand couponCommand = CouponCommand.builder()
                .couponNo(couponNo)
                .userId(userId)
                .build();

            // 사용처리
            couponService.updateCouponUseYn(couponCommand);

//            discountPrice = checkedCoupon.get().get(0).calculateDiscount(totalAmount);
        }

        //포인트 잔액 조회
        PointInfo pointInfo = pointService.getPointInfo(userInfo.getUserId());
        log.debug("::: pointInfo ::: {}", pointInfo);

        if(pointInfo.getPoint() < totalAmount.subtract(discountPrice).intValue()){
            throw new CoreException(ErrorType.BAD_REQUEST, "가지고 있는 잔액이 부족합니다");
        }

        OrderInfo orderInfo = orderService.placeOrder(userId, totalAmount, orderItems, null);

        for(orderItem item : orderItems){   //재고 차감
            productService.orderedStock(item.productId(), item.quantity());
        }

        return OrderResult.of(orderInfo);
    }
}
