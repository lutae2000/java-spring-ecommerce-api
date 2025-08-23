package com.loopers.application.order;

import com.loopers.domain.card.Card;
import com.loopers.domain.card.CardService;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderDetail;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.transaction.support.TransactionTemplate;

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

    /**
     * 주문 생성
     * @param criteria 주문 생성 요청 정보
     * @return 주문 정보
     */
    public OrderInfo placeOrder(OrderCriteria.CreateOrder criteria) {
        BigDecimal discountPrice = BigDecimal.ZERO;

        //회원 유효성 검증
        UserInfo userInfo = userService.getUserInfo(criteria.userId());
        if(org.apache.commons.lang3.ObjectUtils.isEmpty(userInfo)){
            throw new CoreException(ErrorType.BAD_REQUEST, "유효한 계정이 아닙니다");
        }

        //유효상품 검증
        criteria.orderDetails().forEach(detail -> {
            productService.findProduct(detail.productId());
        });

        //쿠폰 적용가능 여부 조회
        if(StringUtils.isNotEmpty(criteria.couponNo())){
            try {
                CouponCommand couponCommand = new CouponCommand(criteria.userId(), criteria.couponNo());
                Coupon coupon = couponService.getCouponAndUse(couponCommand);  // 조회와 사용을 하나의 트랜잭션으로 처리

                //쿠폰 할인금액 계산
                discountPrice = coupon.calculateDiscount(coupon.getDiscountType(),
                    criteria.orderDetails().stream()
                        .map(orderDetail -> orderDetail.unitPrice().multiply(BigDecimal.valueOf(orderDetail.quantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                );
            } catch (CoreException e) {
                // 쿠폰 사용 실패 시 쿠폰 없이 주문 진행
                log.warn("쿠폰 사용 실패: {}, 쿠폰 없이 주문 진행", e.getMessage());
            }
        }

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (OrderCriteria.OrderDetailRequest detail : criteria.orderDetails()) {
            OrderDetail orderDetail = OrderDetail.CreateOrderDetail(
                detail.productId(),
                detail.quantity(),
                detail.unitPrice()
            );
            orderDetails.add(orderDetail);
        }

        Order order = Order.createOrder(criteria.userId(), orderDetails, criteria.couponNo(), discountPrice);

        Long cost = order.getTotalAmount().subtract(discountPrice).longValue();

/*        //포인트 잔액 조회
        Point pointInfo = pointService.getPointInfo(userInfo.getUserId());
        log.debug("::: pointInfo ::: {}", pointInfo);


        if(pointInfo.getPoint() < cost){
            throw new CoreException(ErrorType.BAD_REQUEST, "가지고 있는 잔액이 부족합니다");
        } else{
            pointService.updatePoint(userInfo.getUserId(), cost);
        }*/


/*        for(OrderDetail item : order.getOrderDetailList()){   //재고 차감
            productService.orderedStock(item.getProductId(), item.getQuantity());
        }*/


        OrderInfo orderInfo = orderService.placeOrder(
            criteria.userId(),
            order,
            discountPrice
        );

        Card card = cardService.getCardByUserId(criteria.userId());

        if(ObjectUtils.isEmpty(card)){
            throw new CoreException(ErrorType.NOT_FOUND, "결제할 카드가 미등록되어 있습니다");
        }
        //결제 진행
        paymentService.createPayment(criteria.userId(), orderInfo.getOrder().getOrderNo(), cost, card.getCardType(), card.getCardNo());

        log.info("Order created successfully - orderNo: {}", orderInfo.getOrder().getOrderNo());
        return orderInfo;
    }

    /**
     * 사용자의 모든 주문 조회
     * @param criteria 주문 조회 요청 정보
     * @return 주문 목록
     */
    @Transactional(readOnly = true)
    public List<OrderInfo> getOrdersByUserId(OrderCriteria.GetOrdersByUserId criteria) {
        log.info("Order retrieval requested - userId: {}", criteria.userId());
        
        List<OrderInfo> orders = orderService.findAllOrderByUserId(criteria.userId());
        
        log.info("Orders retrieved - userId: {}, count: {}", criteria.userId(), orders.size());
        return orders;
    }

    /**
     * 주문번호로 주문 상세 조회
     * @param criteria 주문 상세 조회 요청 정보
     * @return 주문 정보
     */
    @Transactional(readOnly = true)
    public OrderInfo getOrderByOrderNo(OrderCriteria.GetOrderByOrderNo criteria) {
        log.info("Order detail retrieval requested - userId: {}, orderNo: {}", 
            criteria.userId(), criteria.orderNo());
        
        try {
            OrderInfo orderInfo = orderService.findOrderInfoByOrderNo(criteria.userId(), criteria.orderNo());
            List<OrderDetail> orderDetails = orderService.findOrderDetailByOrderNo(criteria.orderNo());
            orderInfo.getOrder().setOrderDetailList(orderDetails);

            log.info("Order detail retrieved - orderNo: {}, status: {}", 
                criteria.orderNo(), orderInfo.getOrder().getOrderStatus());
            return orderInfo;
            
        } catch (Exception e) {
            log.error("Order detail retrieval failed - userId: {}, orderNo: {}, error: {}", 
                criteria.userId(), criteria.orderNo(), e.getMessage());
            throw new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다");
        }
    }
}
