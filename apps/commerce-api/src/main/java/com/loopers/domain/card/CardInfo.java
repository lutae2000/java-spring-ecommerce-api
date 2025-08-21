package com.loopers.domain.card;

import com.loopers.interfaces.api.payment.CardType;
import com.loopers.support.utils.CardNumberUtil;
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

    /**
     * 카드번호를 4자리마다 하이픈(-)을 넣어서 반환
     * @return 포맷된 카드번호 (예: "1234-5678-9012-3456")
     */
    public String getFormattedCardNo() {
        return CardNumberUtil.formatCardNumber(cardNo);
    }

    /**
     * 카드번호의 마지막 4자리만 반환 (보안상)
     * @return 마지막 4자리 카드번호 (예: "3456")
     */
    public String getLastFourDigits() {
        return CardNumberUtil.getLastFourDigits(cardNo);
    }

    /**
     * 카드번호를 마스킹 처리하여 반환 (보안상)
     * @return 마스킹된 카드번호 (예: "****-****-****-3456")
     */
    public String getMaskedCardNo() {
        return CardNumberUtil.maskCardNumber(cardNo);
    }
}
