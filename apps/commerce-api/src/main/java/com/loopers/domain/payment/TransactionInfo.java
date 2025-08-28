package com.loopers.domain.payment;

import com.loopers.interfaces.api.payment.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TransactionInfo {
    private String transactionKey;
    private String orderId;
    private Long amount;
    private String reason;
    private TransactionStatus status;
    private CardType cardType;
    private String cardNo;
}
