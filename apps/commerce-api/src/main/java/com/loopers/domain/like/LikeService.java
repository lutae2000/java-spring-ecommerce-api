package com.loopers.domain.like;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {
    private final LikeRepository likeRepository;
    private final LikeSummaryRepository likeSummaryRepository;
    /**
     * like 추가
     */
    @Transactional
    public void like(String userId, String productId){

        List<Like> likeCount = likeRepository.likeByProductId(productId);
        if(likeCount.isEmpty()) {
            likeRepository.save(new Like(userId, productId));
            LikeSummary likeSummary = likeSummaryRepository.likeSummaryByProductId(productId);
            likeSummary.increaseLikesCount();
            likeSummaryRepository.updateLikeSummary(likeSummary);
        }
    }

    /**
     * like 취소
     */
    @Transactional
    public void likeCancel(String userId, String productId){
        likeRepository.deleteByProductIdAndUserId(userId, productId);

        LikeSummary likeSummary = this.likeSummaryByProductId(productId);
        likeSummary.decreaseLikesCount();

        likeSummaryRepository.updateLikeSummary(likeSummary);
    }

    /**
     * Like 카운팅
     * @param userId
     * @param productId
     * @return
     */
    public List<Like> getLikeByProductId(String productId){
        return likeRepository.likeByProductId(productId);
    }

    /**
     * LikeSummary 카운팅
     * @param productId
     * @return
     */
    public LikeSummary likeSummaryByProductId(String productId){
        return likeSummaryRepository.likeSummaryByProductId(productId);
    }
}
