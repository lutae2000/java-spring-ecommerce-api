package org.example.commercebatch.infrastructure;

import org.example.commercebatch.domain.ProductMetricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductMetricRepository extends JpaRepository<ProductMetricsEntity, Long> {
    
    @Query("""
        SELECT pm.productId, p.name, p.brand, p.category1,
               SUM(pm.likesCount), SUM(pm.salesCount), 
               SUM(pm.salesAmount), SUM(pm.pageViews)
        FROM ProductMetricsEntity pm
        INNER JOIN Product p ON pm.productId = p.code
        WHERE pm.metricsDate BETWEEN :startDate AND :endDate
        GROUP BY pm.productId, p.name, p.brand, p.category1
        ORDER BY (
            SUM(pm.salesAmount) * 0.6 + 
            SUM(pm.likesCount) * 0.2 + 
            SUM(pm.pageViews) * 0.1
        ) DESC
        """)
    List<Object[]> findAggregatedByDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
