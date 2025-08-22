package com.loopers.application.payment;

import com.loopers.domain.payment.OrderResponse;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.TransactionDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFacade {
    
    private final PaymentService paymentService;

    /**
     * 결제 생성
     * @param criteria 결제 생성 요청 정보
     * @return 결제 정보
     */
    public PaymentInfo createPayment(PaymentCriteria.CreatePayment criteria) {
        log.info("Payment creation requested - userId: {}, orderId: {}, amount: {}", 
            criteria.userId(), criteria.orderId(), criteria.amount());
        
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
}
