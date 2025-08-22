package com.loopers.domain.order;


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

    @Transactional
    public OrderInfo placeOrder(String userId, Order order, BigDecimal discountPrice){

        orderRepository.save(order);
        orderDetailRepository.saveAll(order.getOrderDetailList());

        log.info("주문이 생성되었습니다. 주문번호: {}, 사용자: {}", order.getOrderNo(), userId);
        return OrderInfo.of(order);
    }

    @Transactional(readOnly = true)
    public List<OrderInfo> findAllOrderByUserId(String userId){
        return orderRepository.findAllOrderByUserId(userId).stream()
            .map(OrderInfo::of)
            .toList();
    }

    @Transactional(readOnly = true)
    public OrderInfo findOrderInfoByOrderNo(String userId, String orderNo){
        return OrderInfo.of(orderRepository.findByOrderNo(userId, orderNo).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<OrderDetail> findOrderDetailByOrderNo(String orderId){
        return orderDetailRepository.findByOrderId(orderId);
    }
}
