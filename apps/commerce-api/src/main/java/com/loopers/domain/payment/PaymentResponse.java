package com.loopers.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String transactionKey;
    private TransactionStatus status;
}
