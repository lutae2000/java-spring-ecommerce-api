package com.loopers.domain.card;

public interface CardRepository {

    /**
     * 회원이 가지고 있는 카드 조회
     * @param userId
     * @return
     */
    Card getCardByUserId(String userId);

    /**
     * 카드정보 저장
     * @param card
     * @return
     */
    Card save(Card card);
}
