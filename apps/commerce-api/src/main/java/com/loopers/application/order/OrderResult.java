package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderInfo;

public record OrderResult (
    OrderInfo orderInfo

){
    public static OrderResult of(OrderInfo orderInfo) {
        return new OrderResult(orderInfo);
    }
}
