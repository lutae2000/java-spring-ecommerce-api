package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface LikeSummaryRepository {

    /**
     * 물품의 좋아요 총 카운팅 저장
     * @param LikeSummary
     */
    void updateLikeSummary(LikeSummary likeSummary);

    /**
     * 물품의 좋아요 카운팅 조회
     * @param productId
     * @return
     */
    LikeSummary likeSummaryByProductId(String productId);

    /**
     * Optimistic locking - 물품의 좋아요 카운팅 조회 (락 없음)
     * @param productId
     * @return
     */
    Optional<LikeSummary> likeSummaryByProductIdOptimistic(String productId);

    /**
     * 물품의 좋아요 갯수 조회
     * @param productId
     * @return
     */
    Long LikeSummaryCountByProductId(String productId);

    /**
     * 여러 상품의 LikeSummary를 여러건 조회
     */
    List<LikeSummary> findByProductCodes(List<String> productCodes);
}
