package com.loopers.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TransactionResponse {
    private String transactionKey;
    private TransactionStatus status;
    private String reason;
}
