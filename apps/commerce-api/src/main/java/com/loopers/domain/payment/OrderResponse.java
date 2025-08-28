package com.loopers.domain.payment;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class OrderResponse {
    private String orderId;
    private List<TransactionResponse> transactions;
}
