package com.loopers.service.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.infrastructure.metrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 상품 메트릭스 업데이트 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductMetricsService {

    private final ProductMetricsRepository productMetricsRepository;

    /**
     * 좋아요 수 업데이트 (upsert)
     */
    @Transactional
    public void updateLikesCount(String productId, Long likesCount, LocalDate metricDate) {
        try {
            // 1. 기존 레코드 조회
            var existingMetrics = productMetricsRepository.findByProductIdAndMetricDate(productId, metricDate);
            
            if (existingMetrics.isPresent()) {
                // 2. 기존 레코드가 있으면 업데이트
                var metrics = existingMetrics.get();
                metrics.updateLikesCount(likesCount);
                productMetricsRepository.save(metrics);
                log.info("기존 좋아요 메트릭스 업데이트 - productId: {}, likesCount: {}, date: {}", 
                        productId, likesCount, metricDate);
            } else {
                // 3. 기존 레코드가 없으면 새로 생성
                var newMetrics = ProductMetrics.createNew(productId, metricDate);
                newMetrics.updateLikesCount(likesCount);
                productMetricsRepository.save(newMetrics);
                log.info("새 좋아요 메트릭스 생성 - productId: {}, likesCount: {}, date: {}", 
                        productId, likesCount, metricDate);
            }
                    
        } catch (Exception e) {
            log.error("좋아요 메트릭스 업데이트 실패 - productId: {}, likesCount: {}, date: {}, error: {}", 
                    productId, likesCount, metricDate, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 판매 정보 업데이트 (upsert)
     */
    @Transactional
    public void updateSalesInfo(String productId, Long salesAmount, LocalDate metricDate) {
        try {
            // 1. 기존 레코드 조회
            var existingMetrics = productMetricsRepository.findByProductIdAndMetricDate(productId, metricDate);

            if (existingMetrics.isPresent()) {
                // 2. 기존 레코드가 있으면 판매 정보 추가
                var metrics = existingMetrics.get();
                metrics.incrementSales(salesAmount);
                productMetricsRepository.save(metrics);
                log.info("기존 판매 메트릭스 업데이트 - productId: {}, salesAmount: {}, date: {}",
                        productId, salesAmount, metricDate);
            } else {
                // 3. 기존 레코드가 없으면 새로 생성하고 판매 정보 추가
                var newMetrics = ProductMetrics.createNew(productId, metricDate);
                newMetrics.incrementSales(salesAmount);
                productMetricsRepository.save(newMetrics);
                log.info("새 판매 메트릭스 생성 - productId: {}, salesAmount: {}, date: {}",
                        productId, salesAmount, metricDate);
            }

        } catch (Exception e) {
            log.error("판매 메트릭스 업데이트 실패 - productId: {}, salesAmount: {}, date: {}, error: {}",
                    productId, salesAmount, metricDate, e.getMessage(), e);
            throw e;
        }
    }
}
