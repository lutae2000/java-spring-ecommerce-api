package com.loopers.domain.order;


import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.OrderDetailCommand.orderItem;
import com.loopers.support.utils.StringUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderInfo placeOrder(String userId, BigDecimal totalAmount, List<orderItem> orderItems, BigDecimal discountPrice){
        //주문번호 채번
        String orderNo = StringUtil.generateCode(7);

        //주문 상세
        List<OrderDetail> details = orderItems.stream()
            .map(item -> new OrderDetail(item.productId(), item.quantity(), item.unitPrice()))
            .collect(Collectors.toList());

        Order order = new Order(orderNo, userId, OrderStatus.ORDER_SUBMIT,totalAmount, details);
        orderRepository.save(order);
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
}
