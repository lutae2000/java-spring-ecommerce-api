package org.example.commercebatch.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mv_product_rank_monthly")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRankMonthly {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "category1", length = 100)
    private String category1;

    @Column(name = "rank_number", nullable = false)
    private Integer rankNumber;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(name = "likes_count", nullable = false)
    private Long likesCount;

    @Column(name = "sales_count", nullable = false)
    private Long salesCount;

    @Column(name = "sales_amount", nullable = false)
    private Long salesAmount;

    @Column(name = "page_views", nullable = false)
    private Long pageViews;

    @Column(name = "month_start_date", nullable = false)
    private LocalDate monthStartDate;

    @Column(name = "month_end_date", nullable = false)
    private LocalDate monthEndDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public ProductRankMonthly(String productId, String productName, String brand, String category1,
                             Integer rankNumber, Double totalScore, Long likesCount, Long salesCount,
                             Long salesAmount, Long pageViews, LocalDate monthStartDate, LocalDate monthEndDate) {
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.category1 = category1;
        this.rankNumber = rankNumber;
        this.totalScore = totalScore;
        this.likesCount = likesCount;
        this.salesCount = salesCount;
        this.salesAmount = salesAmount;
        this.pageViews = pageViews;
        this.monthStartDate = monthStartDate;
        this.monthEndDate = monthEndDate;
        this.createdAt = LocalDateTime.now();
    }
}
