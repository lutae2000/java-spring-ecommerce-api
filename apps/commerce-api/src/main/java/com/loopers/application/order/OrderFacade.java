package com.loopers.application.order;

import com.loopers.domain.card.Card;
import com.loopers.domain.card.CardService;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderDetail;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.CouponUsageEvent;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductInfo;
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
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher eventPublisher;
    /**
     * 주문 생성 (결제 처리 포함)
     */
    @Transactional
    public OrderInfo placeOrder(OrderCriteria.CreateOrder criteria) {
        log.info("주문 생성 시작 - userId: {}", criteria.userId());
        
        try {
            BigDecimal discountPrice;

            // 0. 입력 값 사전 검증 및 기본값 보정
            validateOrderDetails(criteria.orderDetails());
            BigDecimal resolvedUsePoint = criteria.usePoint() == null ? BigDecimal.ZERO : criteria.usePoint();
            if (resolvedUsePoint.compareTo(BigDecimal.ZERO) < 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용 포인트는 0 이상이어야 합니다");
            }

            // 1. 사용자 유효성 검증
            UserInfo userInfo = validateUser(criteria.userId());
            
            // 2. 상품 유효성 검증
            validateProducts(criteria.orderDetails());
            
            // 3. 쿠폰 처리 및 할인금액 계산
            discountPrice = StringUtils.isNotEmpty(criteria.couponNo()) ? processCoupon(criteria) : BigDecimal.ZERO;
            
            // 4. 주문 생성
            Order order = createOrder(criteria, discountPrice);
            
            // 5. 포인트 처리
            processPoint(userInfo, criteria, order.getTotalAmount().subtract(discountPrice));
            
            // 6. 주문 저장 (별도 트랜잭션)
            OrderInfo orderInfo = saveOrderInTransaction(criteria.userId(), order, discountPrice);

            if(StringUtils.isNotEmpty(criteria.couponNo())){
                eventPublisher.publishEvent(new CouponUsageEvent(this, criteria.couponNo(), criteria.userId(), order));
            }
            // 7. 결제 처리 (별도 처리 - 실패해도 주문 데이터는 보존)
            eventPublisher.publishEvent(new OrderCreatedEvent(this, order, criteria.userId()));
            
            log.info("주문 생성 완료 - orderNo: {}, userId: {}", orderInfo.getOrder().getOrderNo(), criteria.userId());
            return orderInfo;
            
        } catch (CoreException e) {
            log.error("주문 생성 실패 - userId: {}, error: {}", criteria.userId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("주문 생성 중 예상치 못한 오류 발생 - userId: {}, error: {}", criteria.userId(), e.getMessage());
            throw new CoreException(ErrorType.INTERNAL_ERROR, "주문 처리 중 오류가 발생했습니다");
        }
    }



    /**
     * 결제 처리 및 재고 차감 (주문 생성 후 별도 처리)
     * @param criteria 주문 생성 요청 정보
     * @param orderInfo 주문 정보
     * @return 결제 성공 여부
     */
    public boolean processPaymentAndUpdateStock(OrderCriteria.CreateOrder criteria, OrderInfo orderInfo) {
        log.info("결제 처리 시작 - orderNo: {}, userId: {}", orderInfo.getOrder().getOrderNo(), criteria.userId());
        
        try {
            // 1. 결제 처리
            boolean paymentSuccess = processPayment(criteria, orderInfo.getOrder(), orderInfo.getOrder().getDiscountAmount());
            
            // 2. 결제 성공 시에만 재고 차감 및 주문 상태 업데이트
            if (paymentSuccess) {
                updateProductStock(orderInfo.getOrder());
                orderService.updateOrderStatus(orderInfo.getOrder().getOrderNo(), criteria.userId(), OrderStatus.ORDER_PAID);
                log.info("결제 및 재고 차감 완료 - orderNo: {}, userId: {}", 
                    orderInfo.getOrder().getOrderNo(), criteria.userId());
            } else {
                orderService.updateOrderStatus(orderInfo.getOrder().getOrderNo(), criteria.userId(), OrderStatus.ORDER_PLACED);
                log.warn("결제 실패로 재고 차감 생략 - orderNo: {}, userId: {}", 
                    orderInfo.getOrder().getOrderNo(), criteria.userId());
            }
            
            return paymentSuccess;
            
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생 - orderNo: {}, userId: {}, error: {}", 
                orderInfo.getOrder().getOrderNo(), criteria.userId(), e.getMessage());
            // 예외 발생 시에도 주문 상태를 실패로 업데이트
            try {
                orderService.updateOrderStatus(orderInfo.getOrder().getOrderNo(), criteria.userId(), OrderStatus.PAYMENT_FAILED);
            } catch (Exception statusUpdateException) {
                log.error("주문 상태 업데이트 실패 - orderNo: {}, error: {}", 
                    orderInfo.getOrder().getOrderNo(), statusUpdateException.getMessage());
            }
            return false;
        }
    }



    /**
     * 사용자 유효성 검증
     */
    private UserInfo validateUser(String userId) {
        UserInfo userInfo = userService.getUserInfo(userId);
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효한 계정이 아닙니다");
        }
        return userInfo;
    }

    /**
     * 상품 유효성 검증
     */
    private void validateProducts(List<OrderCriteria.OrderDetailRequest> orderDetails) {
        orderDetails.forEach(detail -> {
            ProductInfo productInfo = productService.findProduct(detail.productId());
            if(productInfo.getQuantity() < detail.quantity()){
                throw new CoreException(ErrorType.BAD_REQUEST, "주문하려는 상품의 재고가 부족합니다");
            }
        });
    }

    /**
     * 주문 상세 입력값 유효성 검증
     */
    private void validateOrderDetails(List<OrderCriteria.OrderDetailRequest> orderDetails) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상품이 비어있습니다");
        }
        for (OrderCriteria.OrderDetailRequest detail : orderDetails) {
            if (detail == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "주문 상세 항목이 올바르지 않습니다");
            }
            if (detail.productId() == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다");
            }
            if (detail.quantity() <= 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 수량은 1 이상이어야 합니다");
            }
            if (detail.unitPrice() == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 단가는 필수입니다");
            }
            if (detail.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 단가는 0 이상이어야 합니다");
            }
        }
    }


    /**
     * 쿠폰 처리 및 할인금액 계산
     */
    private BigDecimal processCoupon(OrderCriteria.CreateOrder criteria) {
        if (StringUtils.isEmpty(criteria.couponNo())) {
            return BigDecimal.ZERO;
        }

        try {
            CouponCommand couponCommand = new CouponCommand(criteria.userId(), criteria.couponNo(), Boolean.TRUE);
            Coupon coupon = couponService.updateCouponUseYn(couponCommand);

            // 쿠폰 할인금액 계산
            BigDecimal totalAmount = criteria.orderDetails().stream()
                .map(orderDetail -> orderDetail.unitPrice().multiply(BigDecimal.valueOf(orderDetail.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            return coupon.calculateDiscount(coupon.getDiscountType(), totalAmount);
            
        } catch (CoreException e) {
            log.warn("쿠폰 사용 실패: {}, 쿠폰 없이 주문 진행", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * 주문 생성
     */
    private Order createOrder(OrderCriteria.CreateOrder criteria, BigDecimal discountPrice) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        
        for (OrderCriteria.OrderDetailRequest detail : criteria.orderDetails()) {
            OrderDetail orderDetail = OrderDetail.CreateOrderDetail(
                detail.productId(),
                detail.quantity(),
                detail.unitPrice()
            );
            orderDetails.add(orderDetail);
        }

        // Order.createOrder에서 OrderDetail에 Order 객체를 설정하므로 여기서는 설정하지 않음
        Order order = Order.createOrder(criteria.userId(), orderDetails, criteria.couponNo(), discountPrice);
        
        return order;
    }

    /**
     * 포인트 처리
     */
    private void processPoint(UserInfo userInfo, OrderCriteria.CreateOrder criteria, BigDecimal finalAmount) {
        if (criteria.usePoint() == null || criteria.usePoint().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Point pointInfo = pointService.getPointInfo(userInfo.getUserId());
        log.debug("포인트 정보 조회 - userId: {}, point: {}", userInfo.getUserId(), pointInfo.getPoint());

        if (pointInfo.getPoint() < criteria.usePoint().longValue()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "가지고 있는 포인트가 부족합니다");
        }
        
        if (criteria.usePoint().longValue() > finalAmount.longValue()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용하려는 포인트가 결제금액보다 큽니다");
        }

        pointService.updatePoint(userInfo.getUserId(), criteria.usePoint().longValue());
    }

    /**
     * 결제 처리
     * @return 결제 성공 여부
     */
    private boolean processPayment(OrderCriteria.CreateOrder criteria, Order order, BigDecimal discountPrice) {
        try {
            Card card = cardService.getCardByUserId(criteria.userId());
            if (ObjectUtils.isEmpty(card)) {
                log.error("결제할 카드가 미등록되어 있습니다 - userId: {}", criteria.userId());
                return false;
            }

            BigDecimal usePoint = criteria.usePoint() != null ? criteria.usePoint() : BigDecimal.ZERO;
            Long finalAmount = order.getTotalAmount().subtract(discountPrice).subtract(usePoint).longValue();
            
            PaymentInfo paymentInfo = paymentService.createPayment(
                criteria.userId(),
                order.getOrderNo(),
                finalAmount,
                card.getCardType(),
                card.getCardNo()
            );

            if (ObjectUtils.isEmpty(paymentInfo)) {
                log.error("결제 처리에 실패했습니다 - orderNo: {}, userId: {}", order.getOrderNo(), criteria.userId());
                return false;
            }
            
            log.info("결제 처리 성공 - orderNo: {}, userId: {}, amount: {}", order.getOrderNo(), criteria.userId(), finalAmount);
            return true;
            
        } catch (Exception e) {
            log.error("결제 처리 중 예외 발생 - orderNo: {}, userId: {}, error: {}", order.getOrderNo(), criteria.userId(), e.getMessage());
            return false;
        }
    }

    /**
     * 상품 재고 차감
     */
    private void updateProductStock(Order order) {
        for (OrderDetail item : order.getOrderDetailList()) {
            productService.updateStock(item.getProductId(), item.getQuantity(), OrderStatus.ORDER_PLACED);
        }
    }

    /**
     * 주문 저장 (별도 트랜잭션)
     */
    @Transactional
    private OrderInfo saveOrderInTransaction(String userId, Order order, BigDecimal discountPrice) {
        return orderService.placeOrder(userId, order, discountPrice);
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
        
        // OrderDetail lazy loading 문제 해결을 위해 명시적으로 로드
        for (OrderInfo orderInfo : orders) {
            List<OrderDetail> orderDetails = orderService.findOrderDetailByOrderNo(orderInfo.getOrder().getOrderNo());
            orderInfo.getOrder().setOrderDetailList(orderDetails);
        }
        
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
