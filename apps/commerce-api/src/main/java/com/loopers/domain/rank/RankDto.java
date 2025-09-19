package com.loopers.domain.rank;

import com.loopers.application.product.ProductResult;
import java.math.BigDecimal;

public record RankDto() {
    public record RankResponse(
    String productId,
    String productName,
    BigDecimal price,
    String brandName,
    String category,
    Long like
    ){
        public static RankResponse of(ProductResult productResult){
            return new RankResponse(productResult.code(), productResult.name(), productResult.price(), productResult.brandCode(),
                productResult.category1(), productResult.likes());
        }
    }
}
