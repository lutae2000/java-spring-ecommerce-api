package com.loopers.domain.order.event;

import com.loopers.domain.domainEnum.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka 이벤트용 주문 생성 DTO (순환 참조 방지)
 */
@Getter
@Builder
public class OrderCreatedKafkaEvent {
    private final String orderNo;
    private final String userId;
    private final OrderStatus orderStatus;
    private final String couponNo;
    private final BigDecimal totalAmount;
    private final BigDecimal discountAmount;
    private final List<OrderItemEvent> orderItems;
    private final LocalDateTime createdAt;

    /**
     * OrderCreatedEvent로부터 Kafka 이벤트 생성
     */
    public static OrderCreatedKafkaEvent from(OrderCreatedEvent orderCreatedEvent) {
        var order = orderCreatedEvent.getOrder();
        
        List<OrderItemEvent> orderItems = order.getOrderDetailList().stream()
            .map(detail -> OrderItemEvent.builder()
                .productId(detail.getProductId())
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .build())
            .toList();

        return OrderCreatedKafkaEvent.builder()
            .orderNo(order.getOrderNo())
            .userId(orderCreatedEvent.getUserId())
            .orderStatus(order.getOrderStatus())
            .couponNo(order.getCouponNo())
            .totalAmount(order.getTotalAmount())
            .discountAmount(order.getDiscountAmount())
            .orderItems(orderItems)
            .createdAt(order.getCreatedAt().toLocalDateTime())
            .build();
    }

    /**
     * 주문 상품 정보 (순환 참조 없는 단순 DTO)
     */
    @Getter
    @Builder
    public static class OrderItemEvent {
        private final String productId;
        private final Long quantity;
        private final BigDecimal unitPrice;
    }
}
