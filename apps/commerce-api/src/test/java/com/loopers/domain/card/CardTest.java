package com.loopers.domain.card;

import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.interfaces.api.payment.CardType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Card 도메인 테스트")
class CardTest {

    @Nested
    @DisplayName("Card 생성 테스트")
    class CardCreationTest {

        @Test
        @DisplayName("성공 - 정상적인 카드 정보로 카드 생성")
        void createCard_success() {
            // given
            String userId = "user123";
            String cardName = "우리카드";
            CardType cardType = CardType.KB;
            String cardNo = "1234567890123456";

            // when
            Card card = new Card();
            card.setUserId(userId);
            card.setCardName(cardName);
            card.setCardType(cardType);
            card.setCardNo(cardNo);

            // then
            assertThat(card.getUserId()).isEqualTo(userId);
            assertThat(card.getCardName()).isEqualTo(cardName);
            assertThat(card.getCardType()).isEqualTo(cardType);
            assertThat(card.getCardNo()).isEqualTo(cardNo);
        }

        @Test
        @DisplayName("성공 - 모든 CardType으로 카드 생성 가능")
        void createCard_withAllCardTypes_success() {
            // given & when & then
            for (CardType cardType : CardType.values()) {
                Card card = new Card();
                card.setUserId("user123");
                card.setCardName("테스트카드");
                card.setCardType(cardType);
                card.setCardNo("1234567890123456");

                assertThat(card.getCardType()).isEqualTo(cardType);
            }
        }

        @Test
        @DisplayName("성공 - 빈 문자열 카드명으로도 생성 가능")
        void createCard_withEmptyCardName_success() {
            // given
            Card card = new Card();
            card.setUserId("user123");
            card.setCardName("");
            card.setCardType(CardType.KB);
            card.setCardNo("1234567890123456");

            // when & then
            assertThat(card.getCardName()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 긴 카드번호로도 생성 가능")
        void createCard_withLongCardNumber_success() {
            // given
            String longCardNo = "1234567890123456789012345678901234567890";

            // when
            Card card = new Card();
            card.setUserId("user123");
            card.setCardName("테스트카드");
            card.setCardType(CardType.KB);
            card.setCardNo(longCardNo);

            // then
            assertThat(card.getCardNo()).isEqualTo(longCardNo);
        }
    }

    @Nested
    @DisplayName("Card 동등성 테스트")
    class CardEqualityTest {

        @Test
        @DisplayName("성공 - 동일한 정보를 가진 카드는 동등함")
        void cardEquality_sameInfo_success() {
            // given
            Card card1 = new Card();
            card1.setUserId("user123");
            card1.setCardName("우리카드");
            card1.setCardType(CardType.KB);
            card1.setCardNo("1234567890123456");

            Card card2 = new Card();
            card2.setUserId("user123");
            card2.setCardName("우리카드");
            card2.setCardType(CardType.KB);
            card2.setCardNo("1234567890123456");

            // when & then
            assertThat(card1.getUserId()).isEqualTo(card2.getUserId());
            assertThat(card1.getCardName()).isEqualTo(card2.getCardName());
            assertThat(card1.getCardType()).isEqualTo(card2.getCardType());
            assertThat(card1.getCardNo()).isEqualTo(card2.getCardNo());
        }

        @Test
        @DisplayName("성공 - 다른 정보를 가진 카드는 다름")
        void cardEquality_differentInfo_success() {
            // given
            Card card1 = new Card();
            card1.setUserId("user123");
            card1.setCardName("우리카드");
            card1.setCardType(CardType.KB);
            card1.setCardNo("1234567890123456");

            Card card2 = new Card();
            card2.setUserId("user456");
            card2.setCardName("신한카드");
            card2.setCardType(CardType.SAMSUNG);
            card2.setCardNo("6543210987654321");

            // when & then
            assertThat(card1.getUserId()).isNotEqualTo(card2.getUserId());
            assertThat(card1.getCardName()).isNotEqualTo(card2.getCardName());
            assertThat(card1.getCardType()).isNotEqualTo(card2.getCardType());
            assertThat(card1.getCardNo()).isNotEqualTo(card2.getCardNo());
        }
    }

    @Nested
    @DisplayName("Card 정보 변경 테스트")
    class CardModificationTest {

        @Test
        @DisplayName("성공 - 카드명 변경")
        void modifyCardName_success() {
            // given
            Card card = new Card();
            card.setUserId("user123");
            card.setCardName("기존카드명");
            card.setCardType(CardType.KB);
            card.setCardNo("1234567890123456");

            // when
            card.setCardName("새카드명");

            // then
            assertThat(card.getCardName()).isEqualTo("새카드명");
        }

        @Test
        @DisplayName("성공 - 카드 타입 변경")
        void modifyCardType_success() {
            // given
            Card card = new Card();
            card.setUserId("user123");
            card.setCardName("테스트카드");
            card.setCardType(CardType.KB);
            card.setCardNo("1234567890123456");

            // when
            card.setCardType(CardType.SAMSUNG);

            // then
            assertThat(card.getCardType()).isEqualTo(CardType.SAMSUNG);
        }

        @Test
        @DisplayName("성공 - 카드번호 변경")
        void modifyCardNumber_success() {
            // given
            Card card = new Card();
            card.setUserId("user123");
            card.setCardName("테스트카드");
            card.setCardType(CardType.KB);
            card.setCardNo("1234567890123456");

            // when
            card.setCardNo("6543210987654321");

            // then
            assertThat(card.getCardNo()).isEqualTo("6543210987654321");
        }
    }

    @Nested
    @DisplayName("Card 경계값 테스트")
    class CardBoundaryTest {

        @Test
        @DisplayName("성공 - null 값으로도 생성 가능 (JPA에서 처리)")
        void createCard_withNullValues_success() {
            // given & when
            Card card = new Card();

            // then
            assertThat(card.getUserId()).isNull();
            assertThat(card.getCardName()).isNull();
            assertThat(card.getCardType()).isNull();
            assertThat(card.getCardNo()).isNull();
        }

        @Test
        @DisplayName("성공 - 특수문자가 포함된 카드명")
        void createCard_withSpecialCharacters_success() {
            // given
            String cardNameWithSpecialChars = "우리카드!@#$%^&*()";

            // when
            Card card = new Card();
            card.setUserId("user123");
            card.setCardName(cardNameWithSpecialChars);
            card.setCardType(CardType.KB);
            card.setCardNo("1234567890123456");

            // then
            assertThat(card.getCardName()).isEqualTo(cardNameWithSpecialChars);
        }

        @Test
        @DisplayName("성공 - 한글이 포함된 카드명")
        void createCard_withKoreanCharacters_success() {
            // given
            String koreanCardName = "우리카드한글테스트";

            // when
            Card card = new Card();
            card.setUserId("user123");
            card.setCardName(koreanCardName);
            card.setCardType(CardType.KB);
            card.setCardNo("1234567890123456");

            // then
            assertThat(card.getCardName()).isEqualTo(koreanCardName);
        }
    }
}
