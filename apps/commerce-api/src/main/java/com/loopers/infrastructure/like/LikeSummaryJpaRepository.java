package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeSummary;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeSummaryJpaRepository extends JpaRepository<LikeSummary, Long>{

    /**
     * 물품의 좋아요 카운팅 조회
     * @param productId
     * @return
     */
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ls from LikeSummary ls where ls.productId = :productId")
    Optional<LikeSummary> getLikeByProductId(@Param("productId") String productId);

    /**
     * 물품의 좋아요 총 카운팅 저장
     * @param LikeSummary
     */
    @Modifying
    @Query("update LikeSummary ls set ls.likesCount = :likesCount where ls.productId = :productId")
    int updateLikeSummary(@Param("productId") String productId, @Param("likesCount") Long likesCount);
}
