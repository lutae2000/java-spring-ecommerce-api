package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderDetail;
import com.loopers.domain.order.OrderDetailRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDetailRepositoryImpl implements OrderDetailRepository {
    private final OrderDetailJpaRepository orderDetailJpaRepository;

    /**
     * 주문 상세 목록 저장
     * @param orderDetails
     */
    @Override
    public void saveAll(List<OrderDetail> orderDetails) {
        orderDetailJpaRepository.saveAll(orderDetails);
    }
    
    /**
     * 주문 상세 조회
     * @param orderId
     * @param userId
     * @return
     */
    @Override
    public List<OrderDetail> findByOrderId(String orderId) {
        return orderDetailJpaRepository.findByOrderId(orderId);
    }
}
