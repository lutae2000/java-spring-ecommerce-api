package com.loopers.domain.like;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {
    private final LikeRepository likeRepository;
    private final LikeSummaryRepository likeSummaryRepository;
    
    /**
     * like 추가 (Pessimistic Locking)
     */
    public void like(String userId, String productId){
        // 1. 좋아요 존재 여부 확인 (읽기 전용)
        if (checkLikeExists(userId, productId)) {
            log.debug("Like already exists - userId: {}, productId: {}", userId, productId);
            return; // 이미 좋아요를 눌렀으면 무시 (멱등성 보장)
        }

        // 2. 좋아요 생성 및 LikeSummary 업데이트 (원자적 연산)
        createLikeAndUpdateSummary(userId, productId);
    }

    /**
     * like 추가 (Optimistic Locking)
     */
    public void likeOptimistic(String userId, String productId){
        // 1. 좋아요 존재 여부 확인 (읽기 전용)
        if (checkLikeExists(userId, productId)) {
            log.debug("Like already exists - userId: {}, productId: {}", userId, productId);
            return; // 이미 좋아요를 눌렀으면 무시 (멱등성 보장)
        }

        // 2. 좋아요 생성 및 LikeSummary 업데이트 (낙관적 락)
        createLikeAndUpdateSummaryOptimistic(userId, productId);
    }

    /**
     * like 취소
     */
    public void likeCancel(String userId, String productId){
        log.debug("Like cancel request - userId: {}, productId: {}", userId, productId);

        // 1. 좋아요가 존재하는지 확인
        if (!checkLikeExists(userId, productId)) {
            log.debug("Like does not exist - userId: {}, productId: {}", userId, productId);
            return; // 좋아요가 없으면 무시 (멱등성 보장)
        }

        // 2. 좋아요 삭제 및 LikeSummary 업데이트 (원자적 연산)
        deleteLikeAndUpdateSummary(userId, productId);
    }

    /**
     * 좋아요 존재 여부 확인 (읽기 전용)
     */
    @Transactional(readOnly = true)
    private boolean checkLikeExists(String userId, String productId) {
        return likeRepository.existsByUserIdAndProductId(userId, productId);
    }

    /**
     * 좋아요 생성 및 LikeSummary 업데이트 (원자적 연산)
     */
    @Transactional
    private void createLikeAndUpdateSummary(String userId, String productId) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                // 1. 새로운 좋아요 생성
                Like newLike = new Like(productId, userId);
                likeRepository.save(newLike);
                log.debug("Like created - userId: {}, productId: {}", userId, productId);

                // 2. LikeSummary 원자적 업데이트
                updateLikeSummaryAtomically(productId, true);
                return; // 성공하면 종료
                
            } catch (DeadlockLoserDataAccessException | DataIntegrityViolationException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.warn("Like creation failed after {} retries for userId: {}, productId: {}, error: {}", 
                        maxRetries, userId, productId, e.getMessage());
                    throw e;
                }
                log.debug("Retrying like creation for userId: {}, productId: {}, attempt: {}", 
                    userId, productId, retryCount);
                try {
                    Thread.sleep(100 * retryCount); // 지수 백오프
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }

    /**
     * 좋아요 삭제 및 LikeSummary 업데이트 (원자적 연산)
     */
    @Transactional
    private void deleteLikeAndUpdateSummary(String userId, String productId) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                // 1. 좋아요 삭제
                likeRepository.deleteByProductIdAndUserId(userId, productId);
                log.debug("Like deleted - userId: {}, productId: {}", userId, productId);

                // 2. LikeSummary 원자적 업데이트
                updateLikeSummaryAtomically(productId, false);
                return; // 성공하면 종료
                
            } catch (DeadlockLoserDataAccessException | DataIntegrityViolationException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.warn("Like cancellation failed after {} retries for userId: {}, productId: {}, error: {}", 
                        maxRetries, userId, productId, e.getMessage());
                    throw e;
                }
                log.debug("Retrying like cancellation for userId: {}, productId: {}, attempt: {}", 
                    userId, productId, retryCount);
                try {
                    Thread.sleep(100 * retryCount); // 지수 백오프
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }

    /**
     * 좋아요 생성 및 LikeSummary 업데이트 (낙관적 락)
     */
    @Transactional
    private void createLikeAndUpdateSummaryOptimistic(String userId, String productId) {
        int maxRetries = 10; // 낙관적 락은 더 많은 재시도가 필요할 수 있음
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                // 1. 새로운 좋아요 생성
                Like newLike = new Like(productId, userId);
                likeRepository.save(newLike);
                log.debug("Like created (optimistic) - userId: {}, productId: {}", userId, productId);

                // 2. LikeSummary 낙관적 업데이트
                updateLikeSummaryOptimistic(productId, true);
                return; // 성공하면 종료
                
            } catch (DataIntegrityViolationException e) {
                // 중복 키 에러는 무시 (이미 좋아요가 존재함)
                log.debug("Like already exists (optimistic) - userId: {}, productId: {}", userId, productId);
                return;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.warn("Like creation failed after {} retries for userId: {}, productId: {}, error: {}", 
                        maxRetries, userId, productId, e.getMessage());
                    throw e;
                }
                log.debug("Retrying like creation (optimistic) for userId: {}, productId: {}, attempt: {}", 
                    userId, productId, retryCount);
                try {
                    Thread.sleep(50 * retryCount); // 짧은 백오프
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }

    /**
     * LikeSummary 원자적 업데이트
     */
    @Transactional
    private void updateLikeSummaryAtomically(String productId, boolean isIncrease) {
        LikeSummary likeSummary = likeSummaryRepository.likeSummaryByProductId(productId);
        if (isIncrease) {
            likeSummary.increaseLikesCount();
        } else {
            likeSummary.decreaseLikesCount();
        }
        likeSummaryRepository.updateLikeSummary(likeSummary);
    }

    /**
     * LikeSummary 낙관적 업데이트
     */
    @Transactional
    private void updateLikeSummaryOptimistic(String productId, boolean isIncrease) {
        Optional<LikeSummary> likeSummaryOpt = likeSummaryRepository.likeSummaryByProductIdOptimistic(productId);
        LikeSummary likeSummary;
        
        if (likeSummaryOpt.isPresent()) {
            likeSummary = likeSummaryOpt.get();
        } else {
            likeSummary = new LikeSummary(productId, 0L);
        }
        
        if (isIncrease) {
            likeSummary.increaseLikesCount();
        } else {
            likeSummary.decreaseLikesCount();
        }
        likeSummaryRepository.updateLikeSummary(likeSummary);
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
