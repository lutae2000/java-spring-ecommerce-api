package com.loopers.domain.metrics;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 상품별 일일 메트릭스 엔티티
 */
@Entity
@Table(name = "product_metrics", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "metrics_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProductMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Column(name = "metrics_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "likes_count", nullable = false)
    private Long likesCount = 0L;

    @Column(name = "sales_count", nullable = false)
    private Long salesCount = 0L;

    @Column(name = "sales_amount", nullable = false)
    private Long salesAmount = 0L;

    @Column(name = "page_views", nullable = false)
    private Long pageViews = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ProductMetrics createNew(String productId, LocalDate metricDate) {
        ProductMetrics metrics = new ProductMetrics();
        metrics.productId = productId;
        metrics.metricDate = metricDate;
        metrics.likesCount = 0L;
        metrics.salesCount = 0L;
        metrics.salesAmount = 0L;
        metrics.pageViews = 0L;
        // 디버깅을 위해 로그 추가
        System.out.println("Creating ProductMetrics: productId=" + productId + ", metricDate=" + metricDate);
        return metrics;
    }

    public void updateLikesCount(Long likesCount) {
        this.likesCount = likesCount;
    }

    public void incrementSales(Long amount) {
        this.salesCount++;
        this.salesAmount += amount;
    }

    public void incrementPageViews() {
        this.pageViews++;
    }
}