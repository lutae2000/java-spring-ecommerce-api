package com.loopers.domain.rank.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingEvent {
    private String productId;
    private String eventType; // VIEW, LIKE, ORDER
    private Double weight;    // 이벤트별 가중치
    private Double score;     // 계산된 점수
    private Long timestamp;

    /**
     * 조회를 했을때의 랭킹 이벤트
     * @param productId
     * @return
     */
    public static RankingEvent createViewEvent(String productId) {
        return RankingEvent.builder()
            .productId(productId)
            .eventType("VIEW")
            .weight(0.1)
            .score(1.0)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * 좋아요 했을때의 랭킹 이벤트
     * @param productId
     * @return
     */
    public static RankingEvent createLikeEvent(String productId) {
        return RankingEvent.builder()
            .productId(productId)
            .eventType("LIKE")
            .weight(0.2)
            .score(1.0)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * 주문했을때의 랭킹 이벤트
     * @param productId
     * @param orderAmount
     * @return
     */
    public static RankingEvent createOrderEvent(String productId, String userId, Double orderAmount) {
        return RankingEvent.builder()
            .productId(productId)
            .eventType("ORDER")
            .weight(0.6)
            .score(orderAmount) // 주문 금액을 점수로 사용
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
