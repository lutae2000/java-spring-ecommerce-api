package com.loopers.domain.order.event;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderInfo;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 주문 생성 완료
 */
@Getter
public class OrderCreatedEvent extends ApplicationEvent {
    private final Order order;
    private final String userId;

    public OrderCreatedEvent(Object source, Order order, String userId) {
        super(source);
        this.order = order;
        this.userId = userId;
    }
}
