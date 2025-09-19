package com.loopers.domain.rank;

public record RankCommand(
    String date,
    Integer page,
    Integer size
) {

}
