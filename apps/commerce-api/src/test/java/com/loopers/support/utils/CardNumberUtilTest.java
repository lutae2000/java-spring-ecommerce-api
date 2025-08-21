package com.loopers.support.utils;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CardNumberUtil 테스트")
class CardNumberUtilTest {

    @Test
    @DisplayName("성공 - 16자리 카드번호 포맷팅")
    void formatCardNumber_success() {
        // given
        String cardNo = "1234567890123456";

        // when
        String result = CardNumberUtil.formatCardNumber(cardNo);

        // then
        assertThat(result).isEqualTo("1234-5678-9012-3456");
    }

    @Test
    @DisplayName("실패 - null 카드번호")
    void formatCardNumber_failure_null() {
        // given
        String cardNo = null;

        // when
        String result = CardNumberUtil.formatCardNumber(cardNo);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("실패 - 16자리가 아닌 카드번호")
    void formatCardNumber_failure_invalid_length() {
        // given
        String cardNo = "123456789012345"; // 15자리

        // when
        String result = CardNumberUtil.formatCardNumber(cardNo);

        // then
        assertThat(result).isEqualTo(cardNo);
    }

    @Test
    @DisplayName("성공 - 마지막 4자리 추출")
    void getLastFourDigits_success() {
        // given
        String cardNo = "1234567890123456";

        // when
        String result = CardNumberUtil.getLastFourDigits(cardNo);

        // then
        assertThat(result).isEqualTo("3456");
    }

    @Test
    @DisplayName("실패 - 4자리 미만 카드번호")
    void getLastFourDigits_failure_short_length() {
        // given
        String cardNo = "123"; // 3자리

        // when
        String result = CardNumberUtil.getLastFourDigits(cardNo);

        // then
        assertThat(result).isEqualTo(cardNo);
    }

    @Test
    @DisplayName("성공 - 카드번호 마스킹")
    void maskCardNumber_success() {
        // given
        String cardNo = "1234567890123456";

        // when
        String result = CardNumberUtil.maskCardNumber(cardNo);

        // then
        assertThat(result).isEqualTo("****-****-****-3456");
    }

    @Test
    @DisplayName("실패 - 16자리가 아닌 카드번호 마스킹")
    void maskCardNumber_failure_invalid_length() {
        // given
        String cardNo = "123456789012345"; // 15자리

        // when
        String result = CardNumberUtil.maskCardNumber(cardNo);

        // then
        assertThat(result).isEqualTo(cardNo);
    }

    @Test
    @DisplayName("성공 - 유효한 카드번호 검증")
    void isValidCardNumber_success() {
        // given
        String cardNo = "1234567890123456";

        // when
        boolean result = CardNumberUtil.isValidCardNumber(cardNo);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("실패 - 숫자가 아닌 문자가 포함된 카드번호")
    void isValidCardNumber_failure_non_numeric() {
        // given
        String cardNo = "123456789012345a";

        // when
        boolean result = CardNumberUtil.isValidCardNumber(cardNo);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("실패 - 16자리가 아닌 카드번호")
    void isValidCardNumber_failure_invalid_length() {
        // given
        String cardNo = "123456789012345"; // 15자리

        // when
        boolean result = CardNumberUtil.isValidCardNumber(cardNo);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("성공 - 하이픈 제거")
    void removeHyphens_success() {
        // given
        String formattedCardNo = "1234-5678-9012-3456";

        // when
        String result = CardNumberUtil.removeHyphens(formattedCardNo);

        // then
        assertThat(result).isEqualTo("1234567890123456");
    }

    @Test
    @DisplayName("성공 - 하이픈이 없는 카드번호")
    void removeHyphens_success_no_hyphens() {
        // given
        String cardNo = "1234567890123456";

        // when
        String result = CardNumberUtil.removeHyphens(cardNo);

        // then
        assertThat(result).isEqualTo(cardNo);
    }
}
