package com.loopers.domain.order;

import java.util.List;

public interface OrderDetailRepository {

    /**
     * 주문 상세 목록 저장
     * @param orderDetails
     */
    void saveAll(List<OrderDetail> orderDetails);

    /**
     * 주문상세 조회
     * @param orderId
     * @return
     */
    List<OrderDetail> findByOrderId(String orderId);
}
