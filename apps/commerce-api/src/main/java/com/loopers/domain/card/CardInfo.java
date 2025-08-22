package com.loopers.domain.card;

import com.loopers.interfaces.api.payment.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardInfo {
    private Long id;
    private String userId;
    private String cardName;
    private CardType cardType;
    private String cardNo;

    /**
     * Card 엔티티로부터 CardInfo 생성
     */
    public static CardInfo from(Card card) {
        return CardInfo.builder()
            .id(card.getId())
            .userId(card.getUserId())
            .cardName(card.getCardName())
            .cardType(card.getCardType())
            .cardNo(card.getCardNo())
            .build();
    }

}
