package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class LikeRepositoryImpl implements LikeRepository {
    private final LikeJpaRepository likeJpaRepository;

    /**
     * UserId와 ProductID로 좋아요가 된 내역을 찾는다
     *
     * @param userId
     * @param productId
     * @return
     */
    @Override
    public Optional<Like> findByUserIdAndProductId(String userId, String productId) {
        return likeJpaRepository.findByUserIdAndProductId(userId, productId);
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
     * 물품 하나에 대해 좋아요 카운팅
     * @param productId
     * @return
     */
    @Override
    public Optional<Like> likeByProductId(String productId) {
        return likeJpaRepository.likeByProductId(productId);
    }

    /**
     * like 했었는지 여부 확인
     * @param userId
     * @param productId
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public Boolean existsByUserIdAndProductId(String userId, String productId){
        return likeJpaRepository.existsByUserIdAndProductId(userId, productId);
    }
}
