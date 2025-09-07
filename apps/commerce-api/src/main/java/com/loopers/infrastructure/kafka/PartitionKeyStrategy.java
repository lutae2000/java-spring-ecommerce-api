package com.loopers.infrastructure.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Kafka Partition Key 전략
 * 도메인별로 적절한 파티션 키를 생성하여 순서 보장과 부하 분산을 최적화
 */
@Component
@Slf4j
public class PartitionKeyStrategy {

    /**
     * 상품 관련 이벤트 파티션 키 (catalog-events)
     * - 같은 상품의 이벤트는 순서 보장 필요
     * - Key: productId
     */
    public String getCatalogEventPartitionKey(String productId) {
        return "product:" + productId;
    }

    /**
     * 주문 관련 이벤트 파티션 키 (order-events)  
     * - 같은 주문의 이벤트는 순서 보장 필요
     * - Key: orderId
     */
    public String getOrderEventPartitionKey(String orderId) {
        return "order:" + orderId;
    }

    /**
     * 사용자 관련 이벤트 파티션 키
     * - 같은 사용자의 이벤트는 순서 보장 필요
     * - Key: userId (필요시 사용)
     */
    public String getUserEventPartitionKey(String userId) {
        return "user:" + userId;
    }

    /**
     * 복합 키 생성 (상품 + 사용자)
     * - 특정 사용자의 특정 상품 액션은 순서 보장
     * - 예: 좋아요 이벤트 (같은 사용자가 같은 상품에 대한 연속 액션)
     */
    public String getProductUserPartitionKey(String productId, String userId) {
        // 상품 우선으로 파티셔닝 (상품별 집계가 더 중요)
        return "product:" + productId + ":user:" + userId;
    }

    /**
     * 시간 기반 파티션 키 (시계열 데이터용)
     * - 시간 순서가 중요한 메트릭스/로그 이벤트
     * - Key: date + entityId
     */
    public String getTimeBasedPartitionKey(String entityId, String datePrefix) {
        return datePrefix + ":" + entityId;
    }
}
