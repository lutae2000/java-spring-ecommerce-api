package com.loopers.domain.order;


import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    /**
     * 주문 생성 및 저장
     * @param userId 사용자 ID
     * @param order 주문 객체
     * @param discountPrice 할인 금액
     * @return 주문 정보
     */
    @Transactional
    public OrderInfo placeOrder(String userId, Order order, BigDecimal discountPrice){
        // 주문 유효성 검증
        validateOrder(order);
        
        // 주문 저장 (OrderDetail은 cascade로 자동 저장됨)
        Order savedOrder = orderRepository.save(order);

        log.info("주문이 생성되었습니다. 주문번호: {}, 사용자: {}", order.getOrderNo(), userId);
        return OrderInfo.of(savedOrder);
    }

    /**
     * 주문 상태 업데이트
     * @param orderNo 주문번호
     * @param userId 사용자 ID
     * @param status 변경할 주문 상태
     * @return 업데이트된 주문 정보
     */
    @Transactional
    public OrderInfo updateOrderStatus(String orderNo, String userId, OrderStatus status) {
        Order order = orderRepository.findByOrderNo(userId, orderNo)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다"));
        
        order.setOrderStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("주문 상태 업데이트 - orderNo: {}, userId: {}, status: {}", orderNo, userId, status);
        return OrderInfo.of(updatedOrder);
    }

    /**
     * 주문 유효성 검증
     */
    private void validateOrder(Order order) {
        if (order == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 정보가 없습니다");
        }
        if (order.getOrderDetailList() == null || order.getOrderDetailList().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상세 정보가 없습니다");
        }
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 총액이 유효하지 않습니다");
        }
    }

    @Transactional(readOnly = true)
    public List<OrderInfo> findAllOrderByUserId(String userId){
        return orderRepository.findAllOrderByUserId(userId).stream()
            .map(OrderInfo::of)
            .toList();
    }

    @Transactional(readOnly = true)
    public OrderInfo findOrderInfoByOrderNo(String userId, String orderNo){
        return OrderInfo.of(orderRepository.findByOrderNo(userId, orderNo)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다")));
    }

    @Transactional(readOnly = true)
    public List<OrderDetail> findOrderDetailByOrderNo(String orderId){
        return orderDetailRepository.findByOrderId(orderId);
    }
}
