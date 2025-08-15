package com.loopers.domain.order;

import java.util.List;

public interface OrderDetailRepository {

    /**
     * 주문 상세 저장
     * @param orderDetail
     */
    void OrderDetailSave(List<OrderDetail> orderDetail);

    /**
     * 주문상세 조회
     * @param orderId
     * @param userId
     * @return
     */
    List<OrderDetail> findByOrderNoAndUserId(Long orderId, String userId);
}
