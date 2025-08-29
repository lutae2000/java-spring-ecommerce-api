package com.loopers.domain.order.event;

import com.loopers.domain.order.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 쿠폰 사용 이벤트
 */
@Getter
public class CouponUsageEvent extends ApplicationEvent {
    private final String couponNo;
    private final String userId;
    private final Order order;

    public CouponUsageEvent(Object source, String couponNo, String userId, Order order) {
        super(source);
        this.couponNo = couponNo;
        this.userId = userId;
        this.order = order;
    }
}
