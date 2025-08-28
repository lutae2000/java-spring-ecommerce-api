package com.loopers.application.payment;

import com.loopers.interfaces.api.payment.CardType;
import lombok.Builder;

public class PaymentCriteria {

    /**
     * 결제 생성 요청 정보
     */
    @Builder
    public record CreatePayment(
        String userId,
        String orderId,
        Long amount,
        CardType cardType,
        String cardNo
    ) {}

    /**
     * 결제 조회 요청 정보
     */
    public record GetPaymentInfo(
        String userId,
        String transactionKey
    ) {}

    /**
     * 주문 조회 요청 정보
     */
    @Builder
    public record GetTransactionByOrder(
        String userId,
        String orderId
    ) {}
}
