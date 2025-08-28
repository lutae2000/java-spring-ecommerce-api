package com.loopers.domain.payment;

import com.loopers.interfaces.api.payment.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfo {
    private String transactionKey;
    private String userId;
    private String orderId;
    private CardType cardType;
    private String cardNo;
    private Long amount;
    private String callbackUrl;
    private TransactionStatus status;

    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
            payment.getTransactionKey(),
            payment.getUserId(),
            payment.getOrderId(),
            payment.getCardType(),
            payment.getCardNo(),
            payment.getAmount(),
            payment.getCallbackUrl(),
            payment.getStatus()
        );
    }
}
