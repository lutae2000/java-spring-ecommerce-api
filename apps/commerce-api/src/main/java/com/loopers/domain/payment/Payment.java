package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Table(
    name = "payment",
    indexes = {
        @Index(name = "idx_payment_user_transaction", columnList = "user_id, transaction_key"),
        @Index(name = "idx_payment_order_no", columnList = "user_id, order_id"),
        @Index(name = "idx_payment_order_no_transaction", columnList = "user_id, order_id, transaction_key", unique = true)
    }
)
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Payment extends BaseEntity {
    @Id
    @Column(name = "transaction_key", nullable = false, unique = true)
    private String transactionKey;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "card_type", nullable = false)
    private String cardType;

    @Column(name = "card_no", nullable = false)
    private String cardNo;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "reason", nullable = true)
    private String reason;
}
