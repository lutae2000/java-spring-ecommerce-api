package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import java.util.Optional;

public interface OrderRepository {

    /**
     * 주문 생성
     * @param order
     * @return
     */
     Order save(Order order);

    /**
     * 주문서 조회
     */
    Optional<Order> findByOrderNo(String orderNo);
}
