package com.loopers.domain.rank;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RankingEvent {
    private String productId;
    private String eventType; // VIEW, LIKE, ORDER
    private Double weight;    // 이벤트별 가중치
    private Double score;     // 계산된 점수
    private Long timestamp;
}
