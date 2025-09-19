package org.example.commercebatch.infrastructure;

import org.example.commercebatch.domain.ProductRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ProductRankRepository extends JpaRepository<ProductRank, Long> {
    
    @Modifying
    @Query("DELETE FROM ProductRank pr WHERE pr.weekStartDate = :weekStartDate AND pr.weekEndDate = :weekEndDate")
    void deleteByWeekRange(@Param("weekStartDate") LocalDate weekStartDate, @Param("weekEndDate") LocalDate weekEndDate);
}
