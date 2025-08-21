package com.loopers.domain.card;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    /**
     * 회원이 가지고 있는 카드 조회
     * @param userId
     * @return
     */
    public Card getCardByUserId(String userId){
        return cardRepository.getCardByUserId(userId);
    }

    /**
     * 카드정보 저장
     * @param card
     * @return
     */
    public void saveCard(Card card){
        cardRepository.save(card);
    }
}
