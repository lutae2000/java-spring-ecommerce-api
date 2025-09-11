package com.loopers.domain.rank;

import java.util.List;

public record RankingPage (
    String key,
    int page,
    int size,
    long total,
    List<ProductRanking> items
){

}
