package com.loopers.application.order;

import com.loopers.domain.order.OrderInfo;

/**
 * 주문 처리 결과
 */
public record OrderResult(
    OrderInfo orderInfo,
    boolean paymentSuccess
) {
    /**
     * 주문 성공 여부 (결제 성공 여부와 동일)
     */
    public boolean isSuccess() {
        return paymentSuccess;
    }
    
    /**
     * 주문 실패 여부
     */
    public boolean isFailed() {
        return !paymentSuccess;
    }
}
