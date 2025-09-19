package com.loopers.domain.rank;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponseDto {
    private String productId;
    private String productName;
    private BigDecimal price;
    private String brandName;
    private String category;
    private Double score;
    private Integer rank;
}
