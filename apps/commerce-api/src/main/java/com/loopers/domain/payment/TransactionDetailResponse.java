package com.loopers.domain.payment;

import com.loopers.interfaces.api.payment.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TransactionDetailResponse {
    private String transactionKey;
    private String orderId;
    private CardType cardType;
    private String cardNo;
    private Long amount;
    private TransactionStatus status;
    private String reason;
}
