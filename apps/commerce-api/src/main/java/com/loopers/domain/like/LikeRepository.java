package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    Optional<Like> findByUserIdAndProductId(String userId, String productId);
    Like save(Like like);
    void deleteByProductIdAndUserId(String userId, String productId);
    Optional<Like> likeByProductId(String productId);
    Boolean existsByUserIdAndProductId(String userId, String productId);
}
