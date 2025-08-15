package com.loopers.infrastructure.order;

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


    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findByOrderNo(String userId, String orderNo) {
        return orderJpaRepository.findByOrderNo(userId, orderNo);
    }

    @Override
    public List<Order> findAllOrderByUserId(String userId) {
        return orderJpaRepository.findAllByUserId(userId);
    }

}
