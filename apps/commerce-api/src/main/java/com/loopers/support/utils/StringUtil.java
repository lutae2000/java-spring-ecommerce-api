package com.loopers.support.utils;

import java.security.SecureRandom;

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
}
