package com.loopers.application.like;

import com.loopers.domain.like.LikeCommand;

public record LikeCriteria (
    String userId,
    String productId
) {
    public static LikeCommand toCommand(String userId, String couponNo) {
        return new LikeCommand(userId, couponNo);
    }
}
