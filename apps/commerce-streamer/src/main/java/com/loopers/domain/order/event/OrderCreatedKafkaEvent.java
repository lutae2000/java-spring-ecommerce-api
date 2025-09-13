package com.loopers.domain.order.event;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedKafkaEvent {
    private String eventId;
    private String orderNo;
    private String userId;
    private String orderStatus;
    private Long totalAmount;
    private String couponNo;
    private LocalDateTime occurredAt;
    private List<OrderItem> orderItems;
    private BigDecimal discountAmount;

    
    public OrderCreatedKafkaEvent(String orderNo, String userId, String orderStatus, String couponNo, Long totalAmount) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.orderNo = orderNo;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.couponNo = couponNo;
        this.occurredAt = LocalDateTime.now();
    }

    @Getter
    @Setter
    public static class OrderItem{
        private String productId;
        private Long quantity;
        private Long unitPrice;
    }
}


