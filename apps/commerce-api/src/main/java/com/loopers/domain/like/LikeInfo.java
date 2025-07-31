package com.loopers.domain.like;

import com.loopers.domain.brand.Brand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LikeInfo {

    private String productId;
    private String userId;
    private Boolean likeYn;

    public static LikeInfo from(Like like) {
        return new LikeInfo(
            like.getProductId(),
            like.getUserId(),
            like.getLikeYn()
        );
    }

    public record Create(String productId, String userId, Boolean likeYn) {
        public static class createBuilder {
            private String productId;
            private String userId;
            private Boolean likeYn;
        }
    }
}
