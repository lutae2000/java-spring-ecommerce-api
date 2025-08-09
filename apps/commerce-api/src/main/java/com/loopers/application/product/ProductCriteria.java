package com.loopers.application.product;

import com.loopers.domain.product.ProductCommand;
import java.math.BigDecimal;

public record ProductCriteria(
    String code,
    String name,
    BigDecimal price,
    Long quantity,
    String imgURL,
    String description,
    String brandCode,
    String category1,
    String category2,
    String category3
) {
    public static ProductCommand toCommand(ProductCriteria criteria){
        return new ProductCommand(
            criteria.code(),
            criteria.name(),
            criteria.price(),
            criteria.quantity(),
            criteria.imgURL(),
            criteria.description(),
            criteria.brandCode(),
            criteria.category1(),
            criteria.category2(),
            criteria.category3()
        );
    }
}
