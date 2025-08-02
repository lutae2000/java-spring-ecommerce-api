package com.loopers.domain.order;


import java.math.BigDecimal;
import java.util.List;

public class OrderDetailCommand {

    public record orderItem(String productId, Long quantity, BigDecimal unitPrice){
        public static orderItem from(OrderDetail orderDetail){
            return new orderItem(orderDetail.getProductId(), orderDetail.getQuantity(), orderDetail.getUnitPrice());
        }
    }

    public static List<orderItem> fromEntities(List<OrderDetail> details) {
        return details.stream()
            .map(orderItem::from)
            .toList();
    }

}
