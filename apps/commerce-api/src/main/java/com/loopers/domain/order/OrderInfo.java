package com.loopers.domain.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrderInfo {
    private Order order;

    public static OrderInfo of(Order order){
        return new OrderInfo(order);
    }
}
