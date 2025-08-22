package com.loopers.interfaces.api.payment;

import com.loopers.domain.payment.OrderResponse;
import com.loopers.domain.payment.TransactionResponse;
import com.loopers.domain.payment.TransactionStatus;

import java.util.List;

public class PaymentOrderDto {

    /**
     * 주문 응답 (Payment 도메인용)
     */
    public record Response(
        String orderId,
        List<TransactionResponseDto> transactions
    ) {
        public static Response from(OrderResponse response) {
            List<TransactionResponseDto> transactionResponses = response.getTransactions().stream()
                .map(TransactionResponseDto::from)
                .toList();

            return new Response(
                response.getOrderId(),
                transactionResponses
            );
        }
    }

    /**
     * 거래 응답
     */
    public record TransactionResponseDto(
        String transactionKey,
        TransactionStatus status,
        String reason
    ) {
        public static TransactionResponseDto from(TransactionResponse response) {
            return new TransactionResponseDto(
                response.getTransactionKey(),
                response.getStatus(),
                response.getReason()
            );
        }
    }
}
