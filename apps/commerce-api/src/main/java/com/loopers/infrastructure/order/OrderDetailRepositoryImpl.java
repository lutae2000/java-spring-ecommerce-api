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
     * 주문 상세 저장
     * @param orderDetailList
     */
    @Override
    public void OrderDetailSave(List<OrderDetail> orderDetail) {
        orderDetailJpaRepository.saveAll(orderDetail);
    }

    /**
     * 주문 상세 조회
     * @param orderId
     * @param userId
     * @return
     */
    @Override
    public List<OrderDetail> findByOrderNoAndUserId(Long orderNo, String userId) {
        return orderDetailJpaRepository.findByOrderNoAndUserId(orderNo, userId);
    }
}
