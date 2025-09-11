package com.loopers.domain.product.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 상품 재고 부족 이벤트
 * 재고가 0이 되거나 주문 가능 수량이 부족할 때 발생
 */
@Getter
public class StockOutEvent extends ApplicationEvent {
    private final String productId;
    private final String reason;
    private final Long currentStock;
    private final Long requestedQuantity;
    private final LocalDateTime occurredAt;

    public StockOutEvent(Object source, String productId, String reason, Long currentStock, Long requestedQuantity) {
        super(source);
        this.productId = productId;
        this.reason = reason;
        this.currentStock = currentStock;
        this.requestedQuantity = requestedQuantity;
        this.occurredAt = LocalDateTime.now();
    }

    /**
     * 재고 부족 이벤트 생성 (재고 0)
     */
    public static StockOutEvent stockEmpty(Object source, String productId, Long requestedQuantity) {
        return new StockOutEvent(source, productId, "STOCK_EMPTY", 0L, requestedQuantity);
    }

    /**
     * 재고 부족 이벤트 생성 (요청 수량 > 현재 재고)
     */
    public static StockOutEvent insufficientStock(Object source, String productId, Long currentStock, Long requestedQuantity) {
        return new StockOutEvent(source, productId, "INSUFFICIENT_STOCK", currentStock, requestedQuantity);
    }
}
