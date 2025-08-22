package com.loopers.interfaces.api.payment;

import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.interfaces.api.payment.CardType;
import java.time.ZonedDateTime;

public class PaymentDto {

    /**
     * 결제 생성 요청
     */
    public record CreateRequest(
        String orderId,
        Long amount,
        CardType cardType,
        String cardNo
    ) {}

    /**
     * 결제 응답
     */
    public record Response(
        String transactionKey,
        String userId,
        String orderId,
        CardType cardType,
        String cardNo,
        Long amount,
        String callbackUrl,
        TransactionStatus status
    ) {
        public static Response from(PaymentInfo paymentInfo) {
            return new Response(
                paymentInfo.getTransactionKey(),
                paymentInfo.getUserId(),
                paymentInfo.getOrderId(),
                paymentInfo.getCardType(),
                paymentInfo.getCardNo(),
                paymentInfo.getAmount(),
                paymentInfo.getCallbackUrl(),
                paymentInfo.getStatus()
            );
        }
    }
}
