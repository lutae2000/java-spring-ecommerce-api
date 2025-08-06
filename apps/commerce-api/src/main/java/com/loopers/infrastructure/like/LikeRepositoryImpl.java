package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeRepositoryImpl implements LikeRepository {
    private final LikeJpaRepository likeJpaRepository;

    /**
     * UserId와 ProductID로 좋아요가 된 내역을 찾는다
     * @param userId
     * @param productId
     * @return
     */
    @Override
    public Optional<Like> findByUserIdAndProductId(String userId, String productId) {
        return Optional.ofNullable(likeJpaRepository.findByUserIdAndProductId(userId, productId));
    }

    /**
     * Like 저장
     * @param like
     * @return
     */
    @Override
    public Like save(Like like) {
        return likeJpaRepository.save(like);
    }

    /**
     * UserId와 ProductID로 좋아요가 된 내역을 삭제한다
     * @param userId
     * @param productId
     */
    @Override
    public void deleteByProductIdAndUserId(String userId , String productId) {
        likeJpaRepository.deleteByUserIdAndProductId(userId, productId);
    }

    /**
     * 이미 좋아요를 한 내역이 있는지 체크
     * @param userId
     * @param productId
     * @return
     */
    @Override
    public Long countLikeByUserIdAndProductId(String userId, String productId) {
        return likeJpaRepository.countLikeByUserIdAndProductId(userId, productId);
    }

    /**
     * 물품 하나에 대해 좋아요 카운팅
     * @param productId
     * @return
     */
    @Override
    public Long countByProductId(String productId) {
        return likeJpaRepository.countByProductId(productId);
    }
}
