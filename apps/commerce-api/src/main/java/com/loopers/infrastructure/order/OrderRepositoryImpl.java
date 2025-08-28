package com.loopers.infrastructure.order;

import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;

    /**
     * 주문 생성
     * @param order
     * @return
     */
    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    /**
     * 주문서 조회
     */
    @Override
    public Optional<Order> findByOrderNo(String userId, String orderNo) {
        return orderJpaRepository.findByOrderNo(userId, orderNo);
    }

    /**
     * 회원이 주문한 모든 주문 조회
     * @param userId
     * @return
     */
    @Override
    public List<Order> findAllOrderByUserId(String userId) {
        return orderJpaRepository.findAllByUserId(userId);
    }

    /**
     *  주문상태 변경
     */
    @Override
    public int updateOrderStatus(String orderId, OrderStatus orderStatus) {
        return orderJpaRepository.updateOrderStatus(orderId, orderStatus);
    }

    /**
     * PG결제 요청을 해야할 리스트 조회
     */
    @Override
    public List<Order> selectOrderNoByOrderStatus(OrderStatus orderStatus) {
        return orderJpaRepository.selectOrderNoByOrderStatus(orderStatus);
    }
}
