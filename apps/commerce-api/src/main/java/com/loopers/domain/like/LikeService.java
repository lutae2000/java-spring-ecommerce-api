package com.loopers.domain.like;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;

    /**
     * like 추가
     */
    public Long like(String userId, String productId){
        Optional<Like> existLike = likeRepository.findByUserIdAndProductId(userId, productId);
        if(existLike.isEmpty()) {

            Like like = Like.builder()
                .userId(userId)
                .productId(productId)
                .likeYn(true)
                .build();

            likeRepository.save(like);
        }
        return likeRepository.countByProductId(productId);
    }

    /**
     * like 취소
     */
    public void unlike(String userId, String productId){
        likeRepository.deleteByProductIdAndUserId(userId, productId);
    }

    /**
     * Like 카운팅
     * @param userId
     * @param productId
     * @return
     */
    public Long countLike(String productId){
        return likeRepository.countByProductId(productId);
    }
}
