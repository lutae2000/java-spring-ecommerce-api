package com.loopers.interfaces.api.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreateReq {
    private String userId;
    private String orderId;
    private CardType cardType;
    private String cardNo;
    private Long amount;
    private String callbackUrl;
}
