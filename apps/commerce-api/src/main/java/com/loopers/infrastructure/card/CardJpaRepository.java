package com.loopers.infrastructure.card;

import com.loopers.domain.card.Card;
import com.loopers.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardJpaRepository extends JpaRepository<Card, String> {

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
