package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDetailJpaRepository extends JpaRepository<OrderDetail, Long> {



    /**
     * 주문 상세 조회
     * @param orderId
     * @param userId
     * @return
     */
    @Query("SELECT o FROM OrderDetail o WHERE o.order.orderNo = :orderNo")
    List<OrderDetail> findByOrderId(@Param("orderNo") String orderNo);

}
