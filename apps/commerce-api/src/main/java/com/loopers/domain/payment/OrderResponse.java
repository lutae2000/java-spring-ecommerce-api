package com.loopers.domain.payment;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderResponse {
    private String orderId;
    private List<TransactionResponse> transactionResponses;
}
