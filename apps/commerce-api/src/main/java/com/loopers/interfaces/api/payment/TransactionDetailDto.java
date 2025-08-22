package com.loopers.interfaces.api.payment;

import com.loopers.domain.payment.TransactionDetailResponse;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.interfaces.api.payment.CardType;

public class TransactionDetailDto {

    /**
     * 거래 상세 응답
     */
    public record Response(
        String transactionKey,
        String orderId,
        Long amount,
        CardType cardType,
        String cardNo,
        TransactionStatus status,
        String reason
    ) {
        public static Response from(TransactionDetailResponse response) {
            return new Response(
                response.getTransactionKey(),
                response.getOrderId(),
                response.getAmount(),
                response.getCardType(),
                response.getCardNo(),
                response.getStatus(),
                response.getReason()
            );
        }
    }
}
