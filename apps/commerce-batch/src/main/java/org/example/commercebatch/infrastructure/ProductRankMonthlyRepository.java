package org.example.commercebatch.infrastructure;

import org.example.commercebatch.domain.ProductRankMonthly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ProductRankMonthlyRepository extends JpaRepository<ProductRankMonthly, Long> {
    
    @Modifying
    @Query("DELETE FROM ProductRankMonthly pr WHERE pr.monthStartDate = :monthStartDate AND pr.monthEndDate = :monthEndDate")
    void deleteByMonthRange(@Param("monthStartDate") LocalDate monthStartDate, @Param("monthEndDate") LocalDate monthEndDate);
}
