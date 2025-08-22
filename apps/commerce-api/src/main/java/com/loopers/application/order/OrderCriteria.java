package com.loopers.application.order;

import java.math.BigDecimal;
import java.util.List;

public class OrderCriteria {

    /**
     * 주문 생성 요청 정보
     */
    public record CreateOrder(
        String userId,
        List<OrderDetailRequest> orderDetails,
        String couponNo,
        BigDecimal discountAmount
    ) {}

    /**
     * 주문 상세 요청 정보
     */
    public record OrderDetailRequest(
        String productId,
        Long quantity,
        BigDecimal unitPrice
    ) {}

    /**
     * 사용자별 주문 조회 요청 정보
     */
    public record GetOrdersByUserId(
        String userId
    ) {}

    /**
     * 주문번호로 주문 조회 요청 정보
     */
    public record GetOrderByOrderNo(
        String userId,
        String orderNo
    ) {}
}
