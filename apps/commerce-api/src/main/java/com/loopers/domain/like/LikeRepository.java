package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    List<Like> findByUserIdAndProductId(String userId, String productId);
    Like save(Like like);
    void deleteByProductIdAndUserId(String userId, String productId);
    List<Like> likeByProductId(String productId);

}
