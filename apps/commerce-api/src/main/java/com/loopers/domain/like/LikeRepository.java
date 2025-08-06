package com.loopers.domain.like;

import java.util.Optional;

public interface LikeRepository {
    Optional<Like> findByUserIdAndProductId(String userId, String productId);
    Like save(Like like);
    void deleteByProductIdAndUserId(String userId, String productId);
    Long countLikeByUserIdAndProductId(String userId, String productId);
    Long countByProductId(String productId);
}
