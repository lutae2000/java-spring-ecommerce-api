package com.loopers.domain.order.event;

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
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.event.UserActionEvent;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventHandler {
    private final PaymentService paymentService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CardService cardService;
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 쿠폰 사용 처리 (비동기)
     * 주문 생성 후 쿠폰 사용을 별도로 처리
     */
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCouponUsage(CouponUsageEvent event) {

        //사용자 행동 모니터링
        eventPublisher.publishEvent(new UserActionEvent(event.getUserId(), new Object(){}.getClass().getEnclosingMethod().getName(), event.getCouponNo()));

        log.info("쿠폰 사용 이벤트 처리 시작 - couponNo: {}, userId: {}",
            event.getCouponNo(), event.getUserId());

        try {
            // 쿠폰 사용 처리
            CouponCommand couponCommand = new CouponCommand(event.getUserId(), event.getCouponNo(), Boolean.TRUE);
            Coupon coupon = couponService.updateCouponUseYn(couponCommand);

            log.info("쿠폰 사용 완료 - couponNo: {}, userId: {}", event.getCouponNo(), event.getUserId());

        } catch (Exception e) {
            log.error("쿠폰 사용 처리 실패 - couponNo: {}, userId: {}, error: {}", event.getCouponNo(), event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 주문 처리 후 결제 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreate(OrderCreatedEvent event){
        log.info("주문 생성 이벤트 시작: orderNo{}, userId: {}", event.getOrder(), event.getUserId());

        //결제처리
        processPaymentAsync(event);
    }

    /**
     * 결제 처리를 비동기로 실행 (별도 트랜잭션)
     */
    @Async( "taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPaymentAsync(OrderCreatedEvent event) {
        Order order = event.getOrder();

        // 결제 처리
        boolean paymentSuccess = processPayment(order);

        // 결제 완료 이벤트 발행
        eventPublisher.publishEvent(new OrderPaymentCompletedEvent(this, order, paymentSuccess));

    }

    /**
     * 결제 완료 후 재고 차감 및 주문 상태 업데이트 (동기)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(OrderPaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 처리 시작 - orderNo: {}, success: {}", event.getOrder().getOrderNo(), event.isPaymentSuccess());

        // 비동기로 후속 처리 위임
        processPostPaymentAsync(event);
    }

    /**
     * 결제 후 처리를 비동기로 실행 (별도 트랜잭션)
     */
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPostPaymentAsync(OrderPaymentCompletedEvent event) {
        try {
            Order orderInfo = event.getOrder();
            boolean paymentSuccess = event.isPaymentSuccess();

            if (paymentSuccess) {
                // 결제 성공 시 재고 차감 및 주문 상태 업데이트
                updateProductStock(orderInfo);
                orderService.updateOrderStatus(orderInfo.getOrderNo(), orderInfo.getUserId(), OrderStatus.ORDER_PAID);
                log.info("결제 성공 - 재고 차감 및 주문 상태 업데이트 완료 - orderNo: {}", orderInfo.getOrderNo());
            } else {
                // 결제 실패 시 주문 상태만 업데이트
                orderService.updateOrderStatus(orderInfo.getOrderNo(), orderInfo.getUserId(), OrderStatus.ORDER_PAID);
                log.warn("결제 실패 - 주문 상태 업데이트 완료 - orderNo: {}", orderInfo.getOrderNo());
            }

            // 데이터 플랫폼 전송 이벤트 발행 (외부 I/O 분리)
            eventPublisher.publishEvent(new DataPlatformEvent(this, orderInfo, "PAYMENT"));

        } catch (Exception e) {
            log.error("결제 후 처리 중 오류 발생 - orderNo: {}, error: {}",event.getOrder().getOrderNo(), e.getMessage(), e);
        }
    }

    /**
     * 결제 처리
     * @param order
     * @return
     */
    private boolean processPayment(Order order) {
        try {
            Card card = cardService.getCardByUserId(order.getUserId());
            if (ObjectUtils.isEmpty(card)) {
                log.error("결제할 카드가 미등록되어 있습니다 - userId: {}", order.getUserId());
                return false;
            }

            BigDecimal finalAmount = order.getTotalAmount().subtract(order.getDiscountAmount());

            PaymentInfo paymentInfo = paymentService.createPayment(
                card.getUserId(),
                order.getOrderNo(),
                finalAmount.longValue(),
                card.getCardType(),
                card.getCardNo()
            );

            if (ObjectUtils.isEmpty(paymentInfo)) {
                log.error("결제 처리에 실패했습니다 - orderNo: {}, userId: {}", order.getOrderNo(), order.getUserId());
                return false;
            }

            log.info("결제 처리 성공 - orderNo: {}, userId: {}, amount: {}",
                order.getOrderNo(), order.getUserId(), finalAmount);
            return true;
        } catch (Exception e){
            log.error("결제 처리 중 예외 발생 - orderNo: {}, userId: {}, error: {}",
                order.getOrderNo(), order.getUserId(), e.getMessage());
            return false;
        }
    }

    public void updateProductStock(Order order) {
        for (OrderDetail item : order.getOrderDetailList()) {
            productService.updateStock(item.getProductId(), item.getQuantity(), OrderStatus.ORDER_PLACED);
        }
    }



    /**
     * 데이터 플랫폼 전송 처리 (완전히 분리된 비동기 처리)
     */
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDataPlatformEvent(DataPlatformEvent event) {
        try {
            log.info("데이터 플랫폼 전송 시작 - orderNo: {}, eventType: {}",
                event.getOrder().getOrderNo(), event.getEventType());

            // Mock 데이터 플랫폼 전송
            sendToDataPlatform(event.getOrder(), event.getEventType());

            log.info("데이터 플랫폼 전송 완료 - orderNo: {}",
                event.getOrder().getOrderNo());

        } catch (Exception e) {
            log.error("데이터 플랫폼 전송 실패 - orderNo: {}, error: {}",
                event.getOrder().getOrderNo(), e.getMessage(), e);
        }
    }

    /**
     * 데이터 플랫폼 전송
     */
    public void sendToDataPlatform(Order order, String eventType){
        try{
            Thread.sleep(500);
            log.info("Mock 데이터 플랫폼 전송 - orderNo: {}, userId: {}, eventType: {}",
                order.getOrderNo(), order.getUserId(), eventType);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }
}
