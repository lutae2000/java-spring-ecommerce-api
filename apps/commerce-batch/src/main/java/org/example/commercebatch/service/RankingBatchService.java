package org.example.commercebatch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commercebatch.domain.ProductRank;
import org.example.commercebatch.domain.ProductRankMonthly;
import org.example.commercebatch.infrastructure.ProductMetricRepository;
import org.example.commercebatch.infrastructure.ProductRankRepository;
import org.example.commercebatch.infrastructure.ProductRankMonthlyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankingBatchService {

    private final ProductMetricRepository productMetricRepository;
    private final ProductRankRepository productRankRepository;
    private final ProductRankMonthlyRepository productRankMonthlyRepository;

    @Transactional
    public RankingResult processWeeklyRanking(RankingCommand command) {
        try {
            LocalDate targetDate = command.getTargetDate();
            log.info("주간 랭킹 배치 처리 시작 - targetDate: {}", targetDate);

            // 주간 범위 계산 (월요일 ~ 일요일)
            LocalDate weekStart = targetDate.with(java.time.DayOfWeek.MONDAY);
            LocalDate weekEnd = targetDate.with(java.time.DayOfWeek.SUNDAY);

            log.info("주간 범위: {} ~ {}", weekStart, weekEnd);

            // 기존 주간 데이터 삭제
            productRankRepository.deleteByWeekRange(weekStart, weekEnd);

            // 집계 데이터 조회
            List<Object[]> aggregatedData = productMetricRepository.findAggregatedByDateRange(weekStart, weekEnd);
            log.info("집계된 데이터 건수: {}", aggregatedData.size());

            // TOP 100으로 제한
            List<Object[]> top100Data = aggregatedData.stream()
                    .limit(100)
                    .toList();

            // ProductRank 엔티티 생성 및 저장
            List<ProductRank> productRanks = new ArrayList<>();
            int rank = 1;

            for (Object[] data : top100Data) {
                String productId = (String) data[0];
                String productName = (String) data[1];
                String brand = (String) data[2];
                String category1 = (String) data[3];
                Long likesCount = ((Number) data[4]).longValue();
                Long salesCount = ((Number) data[5]).longValue();
                Long pageViews = ((Number) data[7]).longValue();

                // 가중치 수정: order(salesAmount) 0.6, like 0.2, 조회(pageViews) 0.1
                Double totalScore = salesCount * 0.6 + likesCount * 0.2 + pageViews * 0.1;

                ProductRank productRank = ProductRank.builder()
                        .productId(productId)
                        .productName(productName)
                        .brand(brand)
                        .category1(category1)
                        .rankNumber(rank)
                        .totalScore(totalScore)
                        .likesCount(likesCount)
                        .salesCount(salesCount)
                        .pageViews(pageViews)
                        .weekStartDate(weekStart)
                        .weekEndDate(weekEnd)
                        .build();

                productRanks.add(productRank);
                rank++;
            }

            productRankRepository.saveAll(productRanks);
            log.info("주간 랭킹 배치 처리 완료 - 처리 건수: {}", productRanks.size());
            
            return RankingResult.success(productRanks.size());
        } catch (Exception e) {
            log.error("주간 랭킹 배치 처리 실패", e);
            return RankingResult.failure(e.getMessage());
        }
    }

    @Transactional
    public RankingResult processMonthlyRanking(RankingCommand command) {
        try {
            LocalDate targetDate = command.getTargetDate();
            log.info("월간 랭킹 배치 처리 시작 - targetDate: {}", targetDate);

            // 월간 범위 계산
            LocalDate monthStart = targetDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate monthEnd = targetDate.with(TemporalAdjusters.lastDayOfMonth());

            log.info("월간 범위: {} ~ {}", monthStart, monthEnd);

            // 기존 월간 데이터 삭제
            productRankMonthlyRepository.deleteByMonthRange(monthStart, monthEnd);

            // 집계 데이터 조회
            List<Object[]> aggregatedData = productMetricRepository.findAggregatedByDateRange(monthStart, monthEnd);
            log.info("집계된 데이터 건수: {}", aggregatedData.size());

            // TOP 100으로 제한
            List<Object[]> top100Data = aggregatedData.stream()
                    .limit(100)
                    .toList();

            // ProductRankMonthly 엔티티 생성 및 저장
            List<ProductRankMonthly> productRanks = new ArrayList<>();
            int rank = 1;

            for (Object[] data : top100Data) {
                String productId = (String) data[0];
                String productName = (String) data[1];
                String brand = (String) data[2];
                String category1 = (String) data[3];
                Long likesCount = ((Number) data[4]).longValue();
                Long salesCount = ((Number) data[5]).longValue();
                Long salesAmount = ((Number) data[6]).longValue();
                Long pageViews = ((Number) data[7]).longValue();

                // 가중치 수정: order(salesAmount) 0.6, like 0.2, 조회(pageViews) 0.1, salesCount 0.1
                Double totalScore = salesAmount * 0.6 + likesCount * 0.2 + pageViews * 0.1 + salesCount * 0.1;

                ProductRankMonthly productRank = ProductRankMonthly.builder()
                        .productId(productId)
                        .productName(productName)
                        .brand(brand)
                        .category1(category1)
                        .rankNumber(rank)
                        .totalScore(totalScore)
                        .likesCount(likesCount)
                        .salesCount(salesCount)
                        .salesAmount(salesAmount)
                        .pageViews(pageViews)
                        .monthStartDate(monthStart)
                        .monthEndDate(monthEnd)
                        .build();

                productRanks.add(productRank);
                rank++;
            }

            productRankMonthlyRepository.saveAll(productRanks);
            log.info("월간 랭킹 배치 처리 완료 - 처리 건수: {}", productRanks.size());
            
            return RankingResult.success(productRanks.size());
        } catch (Exception e) {
            log.error("월간 랭킹 배치 처리 실패", e);
            return RankingResult.failure(e.getMessage());
        }
    }
}
