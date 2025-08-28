package com.loopers.interfaces.api.payment;

import com.loopers.domain.payment.TransactionStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResp {
    private String transactionKey;
    private String userId;
    private String orderId;
    private CardType cardType;
    private String cardNo;
    private Long amount;
    private String callbackUrl;
    private TransactionStatus status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
