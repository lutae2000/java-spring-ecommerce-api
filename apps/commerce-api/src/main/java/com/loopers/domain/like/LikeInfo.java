package com.loopers.domain.like;

import com.loopers.domain.brand.Brand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LikeInfo {

    private String productId;
    private String userId;
    private Boolean likeYn;
    private Long likesCount;


    public static LikeInfo from(Like like) {
        return new LikeInfo(
            like.getProductId(),
            like.getUserId(),
            like.getLikeYn(),
            ObjectUtils.isEmpty(like.getLikesCount()) ? 0: like.getLikesCount()
        );
    }

    public record Create(String productId, String userId, Boolean likeYn, Long likesCount) {
        public static class createBuilder {
            private String productId;
            private String userId;
            private Boolean likeYn;
            private Long likesCount;
        }
    }
}
