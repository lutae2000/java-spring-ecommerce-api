package com.loopers.domain.order.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.loopers.domain.order.service.OrderEventProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Order 이벤트 핸들러 - 이벤트 라우팅 역할만 담당
 * 
 * 단일 책임 원칙(SRP)에 따라 이벤트 라우팅만 수행하고,
 * 실제 비즈니스 로직은 OrderEventProcessingService에 위임합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventHandler {
    
    private final OrderEventProcessingService orderEventProcessingService;

    /**
     * OrderCreatedEvent를 받아서 적절한 서비스로 라우팅
     */
    @EventListener
    @Async("taskExecutor")
    public void handleOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent) throws JsonProcessingException {
        log.info("OrderCreatedEvent received - orderId: {}, userId: {}", 
                orderCreatedEvent.getOrder().getId(), orderCreatedEvent.getUserId());
        
        // 비즈니스 로직 처리를 서비스에 위임
        orderEventProcessingService.processOrderCreatedEvent(orderCreatedEvent);
    }
}
