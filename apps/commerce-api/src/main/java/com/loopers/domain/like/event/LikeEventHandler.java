package com.loopers.domain.like.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.loopers.domain.like.service.LikeEventProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Like 이벤트 핸들러 - 이벤트 라우팅 역할만 담당
 * 
 * 단일 책임 원칙(SRP)에 따라 이벤트 라우팅만 수행
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LikeEventHandler {
    
    private final LikeEventProcessingService likeEventProcessingService;

    /**
     * LikeEvent를 받아서 적절한 서비스로 라우팅
     */
    @EventListener
    @Async("taskExecutor")
    public void handleLikeEvent(LikeEvent likeEvent) throws JsonProcessingException {
        log.info("LikeEvent received - productId: {}, userId: {}, increment: {}",
                likeEvent.getProductId(), likeEvent.getUserId(), likeEvent.isIncrement());

        likeEventProcessingService.processLikeEvent(likeEvent);
    }
}
