package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeCriteria;
import com.loopers.interfaces.api.product.ProductDto;

public record LikeDto(
    String productId,
    String userId
) {
    public static LikeCriteria toCriteria(LikeCriteria likeCriteria) {
        return new LikeCriteria(
            likeCriteria.userId(),
            likeCriteria.productId()
        );
    }
}
