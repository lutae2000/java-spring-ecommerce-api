package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 상품 메트릭스 Repository
 */
@Repository
public interface ProductMetricsRepository extends JpaRepository<ProductMetrics, Long> {
    
    /**
     * 상품 ID와 날짜로 메트릭스 조회
     */
    Optional<ProductMetrics> findByProductIdAndMetricDate(String productId, LocalDate metricDate);
    
    /**
     * 좋아요 수 업데이트 (upsert)
     */
    @Modifying
    @Query(value = """
        INSERT INTO product_metrics (product_id, metric_date, likes_count, sales_count, sales_amount, page_views, created_at, updated_at)
        VALUES (:productId, STR_TO_DATE(:metricDate, '%Y-%m-%d'), :likesCount, 0, 0, 0, NOW(), NOW())
        ON DUPLICATE KEY UPDATE 
            likes_count = :likesCount,
            updated_at = NOW()
        """, nativeQuery = true)
    void upsertLikesCount(@Param("productId") String productId, 
                         @Param("metricDate") String metricDate, 
                         @Param("likesCount") Long likesCount);
    
    /**
     * 판매 정보 업데이트 (upsert)
     */
    @Modifying
    @Query(value = """
        INSERT INTO product_metrics (product_id, metric_date, likes_count, sales_count, sales_amount, page_views, created_at, updated_at)
        VALUES (:productId, STR_TO_DATE(:metricDate, '%Y-%m-%d'), 0, 1, :salesAmount, 0, NOW(), NOW())
        ON DUPLICATE KEY UPDATE 
            sales_count = sales_count + 1,
            sales_amount = sales_amount + :salesAmount,
            updated_at = NOW()
        """, nativeQuery = true)
    void upsertSalesInfo(@Param("productId") String productId, 
                        @Param("metricDate") String metricDate, 
                        @Param("salesAmount") Long salesAmount);
}