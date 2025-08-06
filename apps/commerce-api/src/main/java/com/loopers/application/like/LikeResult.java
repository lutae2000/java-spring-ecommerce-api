package com.loopers.application.like;

public record LikeResult (
    String productId,
    Long likesCount
){
    public static LikeResult of(String productId, Long likesCount) {
        return new LikeResult(productId, likesCount);
    }
}
