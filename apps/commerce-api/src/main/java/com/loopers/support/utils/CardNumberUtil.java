package com.loopers.support.utils;

import org.springframework.util.StringUtils;

/**
 * 카드번호 관련 유틸리티 클래스
 */
public class CardNumberUtil {

    private static final int STANDARD_CARD_LENGTH = 16;
    private static final int MASK_LENGTH = 4;

    /**
     * 카드번호를 4자리마다 하이픈(-)을 넣어서 반환
     * @param cardNo 카드번호 (16자리)
     * @return 포맷된 카드번호 (예: "1234-5678-9012-3456")
     */
    public static String formatCardNumber(String cardNo) {
        if (!StringUtils.hasText(cardNo) || cardNo.length() != STANDARD_CARD_LENGTH) {
            return cardNo;
        }
        
        return String.format("%s-%s-%s-%s", 
            cardNo.substring(0, 4),
            cardNo.substring(4, 8),
            cardNo.substring(8, 12),
            cardNo.substring(12, 16)
        );
    }

    /**
     * 카드번호의 마지막 4자리만 반환 (보안상)
     * @param cardNo 카드번호
     * @return 마지막 4자리 카드번호 (예: "3456")
     */
    public static String getLastFourDigits(String cardNo) {
        if (!StringUtils.hasText(cardNo) || cardNo.length() < MASK_LENGTH) {
            return cardNo;
        }
        
        return cardNo.substring(cardNo.length() - MASK_LENGTH);
    }

    /**
     * 카드번호를 마스킹 처리하여 반환 (보안상)
     * @param cardNo 카드번호 (16자리)
     * @return 마스킹된 카드번호 (예: "****-****-****-3456")
     */
    public static String maskCardNumber(String cardNo) {
        if (!StringUtils.hasText(cardNo) || cardNo.length() != STANDARD_CARD_LENGTH) {
            return cardNo;
        }
        
        return String.format("****-****-****-%s", 
            cardNo.substring(12, 16)
        );
    }

    /**
     * 카드번호 유효성 검사
     * @param cardNo 카드번호
     * @return 유효한 카드번호인지 여부
     */
    public static boolean isValidCardNumber(String cardNo) {
        if (!StringUtils.hasText(cardNo)) {
            return false;
        }
        
        // 숫자만 포함되어 있는지 확인
        if (!cardNo.matches("\\d+")) {
            return false;
        }
        
        // 16자리인지 확인
        return cardNo.length() == STANDARD_CARD_LENGTH;
    }

    /**
     * 하이픈이 포함된 카드번호에서 하이픈 제거
     * @param formattedCardNo 포맷된 카드번호 (예: "1234-5678-9012-3456")
     * @return 하이픈이 제거된 카드번호 (예: "1234567890123456")
     */
    public static String removeHyphens(String formattedCardNo) {
        if (!StringUtils.hasText(formattedCardNo)) {
            return formattedCardNo;
        }
        
        return formattedCardNo.replace("-", "");
    }
}
