package com.loopers.domain.order.event;

import com.loopers.domain.order.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 결제 완료 이벤트
 */
@Getter
public class OrderPaymentCompletedEvent extends ApplicationEvent {
    private final Order order;
    private final boolean paymentSuccess;

    public OrderPaymentCompletedEvent(Object source, Order order, boolean paymentSuccess) {
        super(source);
        this.order = order;
        this.paymentSuccess = paymentSuccess;
    }
}
