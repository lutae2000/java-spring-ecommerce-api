package com.loopers.application.payment;

import com.loopers.domain.order.OrderDetail;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.OrderResponse;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.TransactionDetailResponse;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.payment.PaymentDto.CreateCallbackRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFacade {
    
    private final PaymentService paymentService;
    private final ProductService productService;
    private final OrderService orderService;

    /**
     * 결제 생성
     * @param criteria 결제 생성 요청 정보
     * @return 결제 정보
     */
    public PaymentInfo createPayment(PaymentCriteria.CreatePayment criteria) {
        log.info("Payment creation requested - userId: {}, orderId: {}, amount: {}", 
            criteria.userId(), criteria.orderId(), criteria.amount());
        
        try {
            PaymentInfo paymentInfo = paymentService.createPayment(
                criteria.userId(), 
                criteria.orderId(), 
                criteria.amount(), 
                criteria.cardType(), 
                criteria.cardNo()
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
     * @param createCallbackRequest
     */
    public void callbackProcess(CreateCallbackRequest createCallbackRequest) {
        log.info("Callback API called - object: {}", createCallbackRequest);
        paymentService.updatePaymentStatus(
            createCallbackRequest.transactionKey(), createCallbackRequest.orderId(), createCallbackRequest.status(), createCallbackRequest.reason()
        );
    }
}
