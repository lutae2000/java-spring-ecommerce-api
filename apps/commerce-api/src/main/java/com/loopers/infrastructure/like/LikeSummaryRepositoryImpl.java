package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeSummaryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LikeSummaryRepositoryImpl implements LikeSummaryRepository {
    private final LikeSummaryJpaRepository likeSummaryJpaRepository;

    /**
     * 물품의 좋아요 총 카운팅 저장 (원자적 연산)
     * @param LikeSummary
     */
    @Override
    @Transactional
    public void updateLikeSummary(LikeSummary likeSummary) {
        // SQL 기반 원자적 업데이트 사용
        int updated = likeSummaryJpaRepository.updateLikeSummary(likeSummary.getProductId(), likeSummary.getLikesCount());
        if(updated == 0){
            // 업데이트된 행이 없으면 새로 생성
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
        return likeSummaryJpaRepository.getLikeByProductIdForUpdate(productId)
            .orElseGet(() -> new LikeSummary(productId, 0L));
    }

    /**
     * Optimistic locking - 물품의 좋아요 카운팅 조회 (락 없음)
     * @param productId
     * @return
     */
    @Override
    public Optional<LikeSummary> likeSummaryByProductIdOptimistic(String productId) {
        return likeSummaryJpaRepository.getLikeByProductIdOptimistic(productId);
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

    /**
     * 여러 상품의 LikeSummary를 여러건 조회
     */
    @Override
    public List<LikeSummary> findByProductCodes(List<String> productCodes) {
        return likeSummaryJpaRepository.findByProductCodes(productCodes);
    }
}
