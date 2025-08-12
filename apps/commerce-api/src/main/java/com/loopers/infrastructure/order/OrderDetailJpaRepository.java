package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderDetailJpaRepository extends JpaRepository<OrderDetail, Long> {

    /**
     * 주문 상세 저장
     * @param orderDetailList
     */
//    void saveAll(List<OrderDetail> orderDetailList);

    /**
     * 주문 상세 조회
     * @param orderId
     * @param userId
     * @return
     */
    @Query("SELECT o FROM OrderDetail o WHERE o.order.orderNo = :orderNo")
    List<OrderDetail> findByOrderNoAndUserId(Long orderNo, String userId);

}
