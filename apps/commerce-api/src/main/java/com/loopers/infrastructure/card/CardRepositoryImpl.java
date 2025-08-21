package com.loopers.infrastructure.card;

import com.loopers.domain.card.Card;
import com.loopers.domain.card.CardRepository;
import com.loopers.domain.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepository {

    private final CardJpaRepository cardJpaRepository;

    /**
     * 결제정보 저장
     * @param Card
     * @return
     */
    @Override
    public Card save(Card card){
        return cardJpaRepository.save(card);
    }

    /**
     * 결제정보 찾기
     * @param userId
     * @return
     */
    @Override
    public Card getCardByUserId(String userId){
        return cardJpaRepository.getCardByUserId(userId);
    }

}
