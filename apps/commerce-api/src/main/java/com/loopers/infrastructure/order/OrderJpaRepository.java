package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
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
