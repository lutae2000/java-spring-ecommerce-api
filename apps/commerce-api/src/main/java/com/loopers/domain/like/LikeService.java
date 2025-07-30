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
        if(existLike.isPresent()){
            return likeRepository.save(existLike.get()).getId();
        } else {    //좋아요가 하나도 없을 때
            return 0L;
        }

    }

    /**
     * like 취소
     */
    public void unlike(String userId, String productId){
        likeRepository.deleteByProductIdAndUserId(userId, productId);
    }
}
