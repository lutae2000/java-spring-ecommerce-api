package com.loopers.domain.like.event;

import com.loopers.domain.like.LikeSummary;
import com.loopers.domain.like.LikeSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class LikeEventHandler {
    private final LikeSummaryRepository likeSummaryRepository;

    /**
     * 좋아요 후 likeSummary 업데이트를 비동기처리
     * @param likeEvent
     */
    @EventListener
    public void handleLike(LikeEvent likeEvent){
        log.info("=== 이벤트 리스너 실행 시작 ===");
        log.info("likeEvent 처리 시작 - productId: {}, userId: {}, increment: {}", 
            likeEvent.getProductId(), likeEvent.getUserId(), likeEvent.isIncrement());
        
        // 비동기로 실제 처리 위임
        processLikeEventAsync(likeEvent);
    }

    /**
     * 실제 LikeSummary 업데이트를 비동기로 처리
     * @param likeEvent
     */
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processLikeEventAsync(LikeEvent likeEvent) {
        log.info("=== 비동기 처리 시작 ===");
        
        try{
            // LikeSummary 조회 또는 생성
            LikeSummary likeSummary = likeSummaryRepository.likeSummaryByProductId(likeEvent.getProductId());
            
            // LikeSummary가 새로 생성된 경우 (count가 0인 경우)
            if (likeSummary.getLikesCount() == 0L) {
                log.info("새로운 LikeSummary 생성 - productId: {}", likeSummary.getProductId());
            } else {
                log.info("기존 LikeSummary 조회 - productId: {}, 현재 count: {}", likeSummary.getProductId(), likeSummary.getLikesCount());
            }
            
            // LikeSummary 업데이트
            if (likeEvent.isIncrement()) {
                likeSummary.increaseLikesCount();
                log.info("좋아요 증가 - productId: {}, 증가 후 count: {}", likeSummary.getProductId(), likeSummary.getLikesCount());
            } else {
                likeSummary.decreaseLikesCount();
                log.info("좋아요 감소 - productId: {}, 감소 후 count: {}", likeSummary.getProductId(), likeSummary.getLikesCount());
            }

            likeSummaryRepository.updateLikeSummary(likeSummary);
            log.info("LikeSummary 업데이트 완료 - productId: {}, 최종 count: {}", likeSummary.getProductId(), likeSummary.getLikesCount());

            log.info("=== 비동기 처리 완료 ===");
        } catch (Exception e) {
            log.error("LikeSummary 집계 업데이트 실패 - productId: {}, error: {}", likeEvent.getProductId(), e.getMessage(), e);
        }
    }
}
