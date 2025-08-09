package com.loopers.domain.like;



public record LikeCommand(
    String userId,
    String productId
) {
    public record Create(String userId, String productId, Long likesCount){
        public Like toEntity(){
            return new Like(userId, productId, likesCount);
        }
    }
}
