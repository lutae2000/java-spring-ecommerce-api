package com.loopers.domain.payment;

import com.loopers.interfaces.api.payment.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCommand {
    private String userId;
    private String orderId;
    private CardType cardType;
    private String cardNo;
    private Long amount;
    private String callbackUrl;
}
