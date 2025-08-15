package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeSummaryRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeSummaryRepositoryImpl implements LikeSummaryRepository {
    private final LikeSummaryJpaRepository likeSummaryJpaRepository;

    /**
     * 물품의 좋아요 총 카운팅 저장
     * @param LikeSummary
     */
    @Override
    public void updateLikeSummary(LikeSummary likeSummary) {
        int updated = likeSummaryJpaRepository.updateLikeSummary(likeSummary.getProductId(), likeSummary.getLikesCount());
        if(updated == 0){
            likeSummaryJpaRepository.save(likeSummary);
        }
    }

    /**
     * 물품의 좋아요 카운팅 조회
     * @param productId
     * @return
     */
    @Override
    public LikeSummary likeSummaryByProductId(String productId) {
        return likeSummaryJpaRepository.getLikeByProductId(productId)
            .orElseGet(() -> new LikeSummary(productId, 0L));
    }


    /**
     * 물품의 좋아요 갯수 조회
     * @param productId
     * @return
     */
    @Override
    public Long LikeSummaryCountByProductId(String productId){
        return likeSummaryJpaRepository.LikeCountByProductId(productId);
    }
}
