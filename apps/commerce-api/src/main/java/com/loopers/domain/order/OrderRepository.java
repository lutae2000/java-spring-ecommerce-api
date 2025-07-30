package com.loopers.domain.order;

public interface OrderRepository {

    /**
     * 주문 생성
     * @param order
     * @return
     */
     Order registerOrder(Order order);
}
