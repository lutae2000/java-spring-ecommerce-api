package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public enum SortBy {
    LATEST, LIKE_DESC, PRICE_ASC, PRICE_DESC;

    public static SortBy from(String sort) {
        if(StringUtils.isEmpty(sort)) {
            return LIKE_DESC;
        }
        return Arrays.stream(values())
            .filter(array -> array.name().equalsIgnoreCase(sort))
            .findFirst()
            .orElse(LIKE_DESC);
    }
}
