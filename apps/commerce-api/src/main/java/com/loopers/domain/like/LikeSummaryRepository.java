package com.loopers.domain.like;

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


}
