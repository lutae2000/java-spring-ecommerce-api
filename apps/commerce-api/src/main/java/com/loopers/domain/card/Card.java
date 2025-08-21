package com.loopers.domain.card;

import com.loopers.domain.BaseEntity;
import com.loopers.interfaces.api.payment.CardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(
    name = "card",
    indexes = {
        @Index(name = "idx_card_user", columnList = "user_id"),
    }
)
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Card extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "card_name", nullable = false)
    private String cardName; // 카드 별칭 (예: "우리카드", "신한카드")

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Column(name = "card_no", nullable = false)
    private String cardNo;
}
