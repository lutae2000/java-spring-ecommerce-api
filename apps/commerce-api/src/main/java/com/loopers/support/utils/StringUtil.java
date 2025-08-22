package com.loopers.support.utils;

import java.security.SecureRandom;
import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    /**
     * 랜덤 코드 채번
     * @param length
     * @return
     */
    public static String generateCode(int length){
        if(length <= 0){
            throw new IllegalArgumentException("생성하려는 길이는 1보다 커야합니다");
        }

        SecureRandom random = new SecureRandom();
        String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++){
            builder.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }

        return builder.toString();
    }

    //카드 번호 사이에 하이픈 추가
    public static String cardNoWithHyphen(String cardNo){
        if(cardNo.length() != 16 || StringUtils.isEmpty(cardNo)){
            return cardNo;
        }

        StringBuilder sb = new StringBuilder();
        for(int i =0; i < cardNo.length(); i++){
            if(i % 4 == 0 && i != 0){
                sb.append("-");
            }
            sb.append(cardNo.charAt(i));
        }
        return sb.toString();
    }
}
