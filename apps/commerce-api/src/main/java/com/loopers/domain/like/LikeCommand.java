package com.loopers.domain.like;



public class LikeCommand {

    private String userId;
    private String productId;
    private Boolean likeYn;

    public static LikeInfo from(Like like){
        return new LikeInfo(
            like.getProductId(),
            like.getUserId(),
            like.getLikeYn(),
            like.getLikesCount()
        );
    }
    public record Create(String userId, String productId, Boolean likeYn, Long likesCount){
        public Like toEntity(){
            return new Like(userId, productId, likeYn, likesCount);
        }
    }
}
