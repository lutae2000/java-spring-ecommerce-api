package com.loopers.application.payment;

import com.loopers.application.payment.PaymentCriteria.CreatePayment;
import com.loopers.domain.card.Card;
import com.loopers.domain.card.CardService;
import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderDetail;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.OrderResponse;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentResponse;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.TransactionDetailResponse;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.PaymentDto.CreateCallbackRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFacade {
    
    private final PaymentService paymentService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CardService cardService;

    /**
     * 결제 생성
     * @param criteria 결제 생성 요청 정보
     * @return 결제 정보
     */
    public PaymentInfo createPayment(PaymentCriteria.CreatePayment criteria) {
        log.info("Payment creation requested - userId: {}, orderId: {}, amount: {}", 
            criteria.userId(), criteria.orderId(), criteria.amount());
        
        try {
            Card card = cardService.getCardByUserId(criteria.userId()); //카드정보 조회

            //결제정보가 아예 없으면 PG로 결제 요청 안보냄
            if(ObjectUtils.isEmpty(card) && criteria.cardNo() == null){
                throw new CoreException(ErrorType.BAD_REQUEST, "결제 수단이 없습니다");
            }

            PaymentInfo paymentInfo = paymentService.createPayment(
                criteria.userId(), 
                criteria.orderId(), 
                criteria.amount(),
                ObjectUtils.isNotEmpty(criteria.cardType()) ? criteria.cardType() : card.getCardType(),
                StringUtils.isNotEmpty(criteria.cardNo()) ? criteria.cardNo() : card.getCardNo()
            );
            
            if (paymentInfo == null) {
                log.warn("Payment creation failed - userId: {}, orderId: {}", criteria.userId(), criteria.orderId());
            } else {
                log.info("Payment created successfully - transactionKey: {}", paymentInfo.getTransactionKey());
            }
            
            return paymentInfo;
        } catch (Exception e) {
            log.error("Payment creation failed with exception - userId: {}, orderId: {}, error: {}", 
                criteria.userId(), criteria.orderId(), e.getMessage());
            throw e; // 예외를 다시 던져서 Circuit Breaker가 감지할 수 있도록 함
        }
    }

    /**
     * 거래번호로 결제 내역 조회
     * @param criteria 결제 조회 요청 정보
     * @return 결제 상세 정보
     */
    public TransactionDetailResponse getPaymentInfo(PaymentCriteria.GetPaymentInfo criteria) {
        log.info("Payment info retrieval requested - userId: {}, transactionKey: {}", 
            criteria.userId(), criteria.transactionKey());
        
        TransactionDetailResponse response = paymentService.getPaymentInfo(
            criteria.userId(), 
            criteria.transactionKey()
        );
        
        log.info("Payment info retrieved - status: {}", response.getStatus());
        return response;
    }

    /**
     * 주문번호로 거래번호 조회
     * @param criteria 주문 조회 요청 정보
     * @return 주문 응답 정보
     */
    public OrderResponse getTransactionByOrder(PaymentCriteria.GetTransactionByOrder criteria) {
        log.info("Transaction retrieval by order requested - userId: {}, orderId: {}", 
            criteria.userId(), criteria.orderId());
        
        OrderResponse response = paymentService.getTransactionByOrder(
            criteria.userId(), 
            criteria.orderId()
        );
        
        log.info("Transaction retrieved by order - orderId: {}", response.getOrderId());
        return response;
    }

    /**
     * PG 콜백받았을때 후속처리
     * @param updatePaymentStatusAndStock
     */
    public void updatePaymentStatusAndStock(CreateCallbackRequest createCallbackRequest) {
        log.info("Callback API called - object: {}", createCallbackRequest);
        paymentService.updatePaymentStatus(
            createCallbackRequest.transactionKey(), createCallbackRequest.orderId(), createCallbackRequest.status(), createCallbackRequest.reason()
        );
        //주문 상세 조회
        List<OrderDetail> orderDetailList = orderService.findOrderDetailByOrderNo(createCallbackRequest.orderId());
        //주문한 수량 재고 차감
        for(OrderDetail orderDetail : orderDetailList){
            productService.updateStock(orderDetail.getProductId(), orderDetail.getQuantity(), OrderStatus.ORDER_PAID);
        }

    }

    /**
     * 결제 상태 확인 및 callback 처리
     */
    public void processPaymentStatusCheck() {
        log.info("결제 상태 확인 시작");
        
        try {
            List<Order> pendingOrders = orderService.selectOrderNoByOrderStatus(OrderStatus.ORDER_PLACED);
            
            log.info("결제 상태 확인 대상 주문 수: {}", pendingOrders.size());
            
            for (Order order : pendingOrders) {
                try {
                    processOrderPaymentStatus(order);
                } catch (Exception e) {
                    log.error("주문 결제 상태 확인 실패 - orderNo: {}, error: {}", 
                        order.getOrderNo(), e.getMessage());
                    // 개별 주문 처리 실패 시에도 다른 주문은 계속 처리
                }
            }
            
        } catch (Exception e) {
            log.error("결제 상태 확인 중 오류 발생: {}", e.getMessage(), e);
            throw new CoreException(ErrorType.INTERNAL_ERROR, "결제 상태 확인 중 오류가 발생했습니다");
        }
    }

    /**
     * 개별 주문의 결제 상태 확인 및 callback 처리
     */
    private void processOrderPaymentStatus(Order order) {
        String orderNo = order.getOrderNo();
        String userId = order.getUserId();
        
        log.debug("주문 결제 상태 확인 - orderNo: {}, userId: {}", orderNo, userId);
        
        try {
            // 1. PG에서 결제 상태 조회
            OrderResponse orderResponse = paymentService.getTransactionByOrder(userId, orderNo);
            if(ObjectUtils.isNotEmpty(orderResponse) && ObjectUtils.isNotEmpty(orderResponse.getTransactions())){
                // 2. 결제 성공 여부 확인
                boolean isPaymentSuccess = orderResponse.getTransactions().stream()
                    .anyMatch(transaction -> transaction.getStatus() == TransactionStatus.SUCCESS);

                if (isPaymentSuccess) {
                    log.info("결제 성공 확인 - orderNo: {}, userId: {}", orderNo, userId);

                    // 3. 결제 성공 시 callback 처리
                    CreateCallbackRequest callbackRequest = CreateCallbackRequest.builder()
                        .orderId(orderNo)
                        .status(TransactionStatus.SUCCESS)
                        .transactionKey(orderResponse.getTransactions().get(0).getTransactionKey())
                        .reason(orderResponse.getTransactions().get(0).getReason())
                        .build();

                    this.updatePaymentStatusAndStock(callbackRequest);

                    log.info("Callback 처리 완료 - orderNo: {}, userId: {}", orderNo, userId);
                }
            } else if(orderResponse.getTransactions() == null){
                log.debug("결제 미완료이므로 결제 진행 - orderNo: {}, userId: {}", orderNo, userId);

                //결제 상태 없으니까 결제 진행
                PaymentCriteria.CreatePayment createPayment = CreatePayment.builder()
                    .orderId(orderNo)
                    .userId(userId)
                    .amount(order.getTotalAmount().longValue())
                    .build();
                this.createPayment(createPayment);
            }
            
        } catch (Exception e) {
            log.error("주문 결제 상태 확인 실패 - orderNo: {}, userId: {}, error: {}", orderNo, userId, e.getMessage());
            throw e;
        }
    }
}
