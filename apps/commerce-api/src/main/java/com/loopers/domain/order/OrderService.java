package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.OrderDetailCommand.orderItem;
import com.loopers.support.utils.StringUtil;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderInfo orderSubmit(String userId, BigDecimal totalAmount, List<orderItem> orderItems){
        String orderNo = StringUtil.generateCode(7);
        List<OrderDetail> details = orderItems.stream()
            .map(item -> new OrderDetail(item.productId(), item.quantity(), item.unitPrice()))
            .collect(Collectors.toList());

        Order order = new Order(orderNo, userId, OrderStatus.ORDER_SUBMIT,totalAmount, details);
        orderRepository.save(order);
        return OrderInfo.of(order);
    }
}
