package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductCriteria;
import java.math.BigDecimal;

public record ProductDto(
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
    public static ProductCriteria toCriteria(ProductDto productDto){
        return new ProductCriteria(
            productDto.code(),
            productDto.name(),
            productDto.price(),
            productDto.quantity(),
            productDto.imgURL(),
            productDto.description(),
            productDto.brandCode(),
            productDto.category1(),
            productDto.category2(),
            productDto.category3()
        );
    }
}
