package com.loopers.domain.order;


import java.math.BigDecimal;
import java.util.List;

public class OrderDetailCommand {

    public record orderItem(String productId, Long quantity, BigDecimal unitPrice){}

}
