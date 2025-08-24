package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.domainEnum.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

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
    Optional<Order> findByOrderNo(String userId, String orderNo);

    /**
     * 회원이 주문한 모든 주문 조회
     * @param userId
     * @return
     */
    List<Order> findAllOrderByUserId(String userId);

    /**
     * 결제 완료후 주문상태 변경
     */
    int updateOrderStatus(String orderNo, OrderStatus orderStatus);

    /**
     * PG결제 요청을 해야할 리스트 조회
     */
    List<Order> selectOrderNoByOrderStatus(OrderStatus orderStatus);
}
