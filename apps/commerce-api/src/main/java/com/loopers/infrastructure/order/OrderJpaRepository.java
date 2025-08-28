package com.loopers.infrastructure.order;

import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.orderNo = :orderNo")
    Optional<Order> findByOrderNo(@Param("userId") String userId, @Param("orderNo") String orderNo);

    /**
     * 특정 회원이 주문한 모든 주문서
     * @param userId
     * @return
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId")
    List<Order> findAllByUserId(@Param("userId") String userId);

    /**
     *  주문상태 변경
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Order o set o.orderStatus = :orderStatus, o.updatedAt = CURRENT_TIMESTAMP where o.orderNo = :orderId")
    int updateOrderStatus(@Param("orderId") String orderId, @Param("orderStatus") OrderStatus orderStatus);

    /**
     * PG결제 요청을 해야할 리스트 조회
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN :orderStatus")
    List<Order> selectOrderNoByOrderStatus(@Param("orderStatus") OrderStatus orderStatus);
}
