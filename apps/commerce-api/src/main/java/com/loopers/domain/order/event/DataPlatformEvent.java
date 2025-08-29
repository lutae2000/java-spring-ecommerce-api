package com.loopers.domain.order.event;

import com.loopers.domain.order.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 데이터 플랫폼 전송 이벤트
 */
@Getter
public class DataPlatformEvent extends ApplicationEvent {
    private final Order order;
    private final String eventType; // "ORDER", "PAYMENT"

    public DataPlatformEvent(Object source, Order order, String eventType) {
        super(source);
        this.order = order;
        this.eventType = eventType;
    }
}
