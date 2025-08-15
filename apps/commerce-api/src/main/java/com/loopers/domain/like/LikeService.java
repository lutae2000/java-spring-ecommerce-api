package com.loopers.domain.like;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

        // 1. 이미 좋아요를 눌렀는지 확인
        Optional<Like> existingLike = likeRepository.findByUserIdAndProductId(userId, productId);
        if(existingLike.isPresent()){
            log.debug("Like already exists - userId: {}, productId: {}", userId, productId);
            return; // 이미 좋아요를 눌렀으면 무시 (멱등성 보장)
        }

        // 2. 새로운 좋아요 생성
        Like newLike = new Like(productId, userId);
        likeRepository.save(newLike);
        log.debug("Like created - userId: {}, productId: {}", userId, productId);

        LikeSummary likeSummary = likeSummaryRepository.likeSummaryByProductId(productId);

        likeSummary.increaseLikesCount();
        likeSummaryRepository.updateLikeSummary(likeSummary);
    }

    /**
     * like 취소
     */
    @Transactional
    public void likeCancel(String userId, String productId){
        log.debug("Like cancel request - userId: {}, productId: {}", userId, productId);

        // 1. 좋아요가 존재하는지 확인
        Optional<Like> existingLike = likeRepository.findByUserIdAndProductId(userId, productId);
        if(existingLike.isPresent()){
            log.debug("Like does not exist - userId: {}, productId: {}", userId, productId);
            return; // 좋아요가 없으면 무시 (멱등성 보장)
        }

        // 2. 좋아요 삭제
        likeRepository.deleteByProductIdAndUserId(userId, productId);
        log.debug("Like deleted - userId: {}, productId: {}", userId, productId);
    }


    /**
     * Like 카운팅
     * @param userId
     * @param productId
     * @return
     */
    @Transactional
    public Optional<Like> getLikeByProductId(String productId){
        return likeRepository.likeByProductId(productId);
    }

    /**
     * LikeSummary 카운팅
     * @param productId
     * @return
     */
    @Transactional
    public LikeSummary likeSummaryByProductId(String productId){
        return likeSummaryRepository.likeSummaryByProductId(productId);
    }

    /**
     * 좋아요 존재여부 확인
     * @param userId
     * @param productId
     * @return
     */
    @Transactional(readOnly = true)
    public Boolean likeExist(String userId, String productId){
        return likeRepository.existsByUserIdAndProductId(userId, productId);
    }

    /**
     * 물품의 좋아요 갯수 조회
     * @param productId
     * @return
     */
    @Transactional(readOnly = true)
    public Long LikeSummaryCountByProductId(String productId){
        return likeSummaryRepository.LikeSummaryCountByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> findLikeSummaryByProductCodes(List<String> productId){

        List<LikeSummary> likeSummaryList = likeSummaryRepository.findByProductCodes(productId);
        return likeSummaryList.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    LikeSummary::getProductId,
                    LikeSummary::getLikesCount
                )
            );
    }
}
