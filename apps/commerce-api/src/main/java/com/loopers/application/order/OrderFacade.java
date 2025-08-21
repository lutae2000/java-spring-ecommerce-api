package com.loopers.application.order;

import com.loopers.domain.card.Card;
import com.loopers.domain.card.CardService;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.domainEnum.DiscountType;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderDetail;
import com.loopers.domain.order.OrderDetailCommand.orderItem;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.Point;
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
    private final PaymentService paymentService;
    private final CardService cardService;

//    @Transactional
    public OrderResult orderSubmit(String userId, Order order){
        BigDecimal discountPrice = BigDecimal.ZERO;

        //회원 유효성 검증
        UserInfo userInfo = userService.getUserInfo(userId);
        if(ObjectUtils.isEmpty(userInfo)){
            throw new CoreException(ErrorType.BAD_REQUEST, "유효한 계정이 아닙니다");
        }

        //쿠폰 적용가능 여부 조회
        if(StringUtils.isNotEmpty(order.getCouponNo())){
            try {
                CouponCommand couponCommand = new CouponCommand(userId, order.getCouponNo());
                Coupon coupon = couponService.getCouponAndUse(couponCommand);  // 조회와 사용을 하나의 트랜잭션으로 처리

                //쿠폰 할인금액 계산
                discountPrice = coupon.calculateDiscount(coupon.getDiscountType(),
                    order.getOrderDetailList().stream()
                        .map(orderDetail -> orderDetail.getUnitPrice().multiply(BigDecimal.valueOf(orderDetail.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                );
            } catch (CoreException e) {
                // 쿠폰 사용 실패 시 쿠폰 없이 주문 진행
                log.warn("쿠폰 사용 실패: {}, 쿠폰 없이 주문 진행", e.getMessage());
            }
        }

        Long cost = order.getTotalAmount().subtract(discountPrice).longValue();
        //포인트 잔액 조회
/*        Point pointInfo = pointService.getPointInfo(userInfo.getUserId());
        log.debug("::: pointInfo ::: {}", pointInfo);


        if(pointInfo.getPoint() < cost){
            throw new CoreException(ErrorType.BAD_REQUEST, "가지고 있는 잔액이 부족합니다");
        } else{
            pointService.updatePoint(userInfo.getUserId(), cost);
        }*/



        for(OrderDetail item : order.getOrderDetailList()){   //재고 차감
            productService.orderedStock(item.getProductId(), item.getQuantity());
        }

        OrderInfo orderInfo = orderService.placeOrder(userId, order, discountPrice);

        Card card = cardService.getCardByUserId(userId);

        if(ObjectUtils.isEmpty(card)){
            throw new CoreException(ErrorType.NOT_FOUND, "결제할 카드가 미등록되어 있습니다");
        }
        //결제 진행
        paymentService.createPayment(userId, orderInfo.getOrder().getOrderNo(), cost, card.getCardType(), card.getCardNo());

        return OrderResult.of(orderInfo);
    }
}
