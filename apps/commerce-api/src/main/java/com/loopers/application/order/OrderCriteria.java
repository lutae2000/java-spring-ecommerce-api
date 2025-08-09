package com.loopers.application.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderCriteria(
    String userId,
    List<OrderDetail> orderDetail,
    String couponNo,
    BigDecimal totalAmount
){
    public record OrderDetail(String productId, Long quantity, BigDecimal unitPrice){}
}
