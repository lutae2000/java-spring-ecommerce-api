package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Arrays;

public enum SortBy {
    LATEST, LIKES_DESC, PRICE_ASC, PRICE_DESC;

    public static SortBy from(String sort) {
        return Arrays.stream(values())
            .filter(s -> s.name().equalsIgnoreCase(sort))
            .findFirst()
            .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "지원하지 않는 정렬입니다"));
    }
}
