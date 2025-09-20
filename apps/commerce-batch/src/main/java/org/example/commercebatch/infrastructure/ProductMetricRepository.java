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
        SELECT pm.productId, SUM(PM.likesCount) likesCount
        , SUM(pm.salesCount) SALES_COUNT, SUM(pm.pageViews) PAGE_VIEWS
        , SUM(pm.salesAmount) salesAmount
        FROM ProductMetricsEntity PM
        WHERE PM.metricsDate BETWEEN :STARTDATE AND :ENDDATE
        GROUP BY pm.productId, PM.likesCount, pm.salesCount, pm.pageViews
        """)
    List<Object[]> findAggregatedByDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
