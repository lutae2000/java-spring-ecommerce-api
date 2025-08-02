package com.loopers.domain.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {
    private final LikeRepository likeRepository;

    /**
     * like 추가
     */
    public LikeInfo like(String userId, String productId){

        try{
            Like like = Like.builder()
                .userId(userId)
                .productId(productId)
                .likeYn(true)
                .build();

            likeRepository.save(like);
        } catch (DataIntegrityViolationException e) {
            //이미 존재해서 저장 실패한 경우 무시하고 진행
            log.debug("::: DataIntegrityViolationException ::: {}", e.getMessage());
        }

        Long likeCount = likeRepository.countByProductId(productId);

        return LikeInfo.builder()
            .productId(productId)
            .likesCount(likeCount)
            .userId(userId)
            .build();
    }

    /**
     * like 취소
     */
    public LikeInfo likeCancel(String userId, String productId){
        likeRepository.deleteByProductIdAndUserId(userId, productId);

        Long likeCount = likeRepository.countByProductId(productId);
        return LikeInfo.builder()
            .productId(productId)
            .likesCount(likeCount)
            .userId(userId)
            .build();
    }

    /**
     * Like 카운팅
     * @param userId
     * @param productId
     * @return
     */
    public Long countLike(String productId){
        return likeRepository.countByProductId(productId);
    }
}
