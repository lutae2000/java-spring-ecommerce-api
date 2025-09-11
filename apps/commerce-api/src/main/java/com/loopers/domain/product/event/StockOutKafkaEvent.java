package com.loopers.domain.product.event;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Kafka용 재고 부족 이벤트 DTO
 */
@Getter
@Builder
public class StockOutKafkaEvent {
    private final String productId;
    private final String reason;
    private final Long currentStock;
    private final Long requestedQuantity;
    private final LocalDateTime occurredAt;

    /**
     * StockOutEvent로부터 Kafka 이벤트 생성
     */
    public static StockOutKafkaEvent from(StockOutEvent stockOutEvent) {
        return StockOutKafkaEvent.builder()
            .productId(stockOutEvent.getProductId())
            .reason(stockOutEvent.getReason())
            .currentStock(stockOutEvent.getCurrentStock())
            .requestedQuantity(stockOutEvent.getRequestedQuantity())
            .occurredAt(stockOutEvent.getOccurredAt())
            .build();
    }
}
