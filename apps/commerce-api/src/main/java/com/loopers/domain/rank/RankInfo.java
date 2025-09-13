package com.loopers.domain.rank;

import java.math.BigDecimal;


public record RankInfo(
    String productId,
    String productName,
    BigDecimal price,
    String brandName,
    String category,
    Long like
) {

}
