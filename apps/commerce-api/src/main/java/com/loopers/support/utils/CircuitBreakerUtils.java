package com.loopers.support.utils;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CircuitBreakerUtils {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Circuit Breaker 상태 로깅
     * @param serviceName 서비스 이름
     */
    public void logCircuitBreakerStatus(String serviceName) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            log.info("Circuit Breaker [{}] - State: {}, Failure Rate: {}%, Total Calls: {}, Failed Calls: {}, Successful Calls: {}",
                serviceName,
                circuitBreaker.getState(),
                circuitBreaker.getMetrics().getFailureRate(),
                circuitBreaker.getMetrics().getNumberOfSuccessfulCalls() + circuitBreaker.getMetrics().getNumberOfFailedCalls(),
                circuitBreaker.getMetrics().getNumberOfFailedCalls(),
                circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
        } catch (Exception e) {
            log.warn("Failed to log circuit breaker status for {}: {}", serviceName, e.getMessage());
        }
    }

    /**
     * Circuit Breaker가 열려있는지 확인
     * @param serviceName 서비스 이름
     * @return 열려있으면 true, 닫혀있으면 false
     */
    public boolean isCircuitBreakerOpen(String serviceName) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            return circuitBreaker.getState() == CircuitBreaker.State.OPEN;
        } catch (Exception e) {
            log.warn("Failed to check circuit breaker state for {}: {}", serviceName, e.getMessage());
            return false;
        }
    }

    /**
     * Circuit Breaker가 반열림 상태인지 확인
     * @param serviceName 서비스 이름
     * @return 반열림 상태면 true, 아니면 false
     */
    public boolean isCircuitBreakerHalfOpen(String serviceName) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            return circuitBreaker.getState() == CircuitBreaker.State.HALF_OPEN;
        } catch (Exception e) {
            log.warn("Failed to check circuit breaker state for {}: {}", serviceName, e.getMessage());
            return false;
        }
    }

    /**
     * Circuit Breaker가 닫혀있는지 확인
     * @param serviceName 서비스 이름
     * @return 닫혀있으면 true, 열려있으면 false
     */
    public boolean isCircuitBreakerClosed(String serviceName) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            return circuitBreaker.getState() == CircuitBreaker.State.CLOSED;
        } catch (Exception e) {
            log.warn("Failed to check circuit breaker state for {}: {}", serviceName, e.getMessage());
            return false;
        }
    }

    /**
     * Circuit Breaker 상태를 문자열로 반환
     * @param serviceName 서비스 이름
     * @return Circuit Breaker 상태 문자열
     */
    public String getCircuitBreakerState(String serviceName) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            return circuitBreaker.getState().name();
        } catch (Exception e) {
            log.warn("Failed to get circuit breaker state for {}: {}", serviceName, e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Circuit Breaker 메트릭 정보를 Map으로 반환
     * @param serviceName 서비스 이름
     * @return Circuit Breaker 메트릭 정보
     */
    public Map<String, Object> getCircuitBreakerMetrics(String serviceName) {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            CircuitBreaker.Metrics circuitMetrics = circuitBreaker.getMetrics();
            
            metrics.put("name", serviceName);
            metrics.put("state", circuitBreaker.getState().name());
            metrics.put("failureRate", circuitMetrics.getFailureRate());
            metrics.put("totalCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls() + circuitBreaker.getMetrics().getNumberOfFailedCalls());
            metrics.put("failedCalls", circuitMetrics.getNumberOfFailedCalls());
            metrics.put("successfulCalls", circuitMetrics.getNumberOfSuccessfulCalls());
            metrics.put("notPermittedCalls", circuitMetrics.getNumberOfNotPermittedCalls());
            
        } catch (Exception e) {
            log.warn("Failed to get circuit breaker metrics for {}: {}", serviceName, e.getMessage());
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }

    /**
     * Circuit Breaker가 동작했는지 확인 (실패율이 임계값을 넘었는지)
     * @param serviceName 서비스 이름
     * @param threshold 실패율 임계값 (기본값: 50.0)
     * @return 동작했으면 true, 아니면 false
     */
    public boolean hasCircuitBreakerActivated(String serviceName, double threshold) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            double failureRate = circuitBreaker.getMetrics().getFailureRate();
            long totalCalls = circuitBreaker.getMetrics().getNumberOfSuccessfulCalls() + circuitBreaker.getMetrics().getNumberOfFailedCalls();
            
            // 최소 호출 수가 충족되고 실패율이 임계값을 넘었는지 확인
            return totalCalls >= 5 && failureRate >= threshold;
        } catch (Exception e) {
            log.warn("Failed to check circuit breaker activation for {}: {}", serviceName, e.getMessage());
            return false;
        }
    }

    /**
     * Circuit Breaker가 동작했는지 확인 (기본 임계값 50% 사용)
     * @param serviceName 서비스 이름
     * @return 동작했으면 true, 아니면 false
     */
    public boolean hasCircuitBreakerActivated(String serviceName) {
        return hasCircuitBreakerActivated(serviceName, 50.0);
    }

    /**
     * Circuit Breaker 리셋
     * @param serviceName 서비스 이름
     * @return 리셋 성공 여부
     */
    public boolean resetCircuitBreaker(String serviceName) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            circuitBreaker.reset();
            log.info("Circuit Breaker [{}] has been reset", serviceName);
            return true;
        } catch (Exception e) {
            log.warn("Failed to reset circuit breaker for {}: {}", serviceName, e.getMessage());
            return false;
        }
    }

    /**
     * 모든 Circuit Breaker 상태 로깅
     */
    public void logAllCircuitBreakers() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            String name = circuitBreaker.getName();
            logCircuitBreakerStatus(name);
        });
    }

    /**
     * Circuit Breaker 상태 요약 정보 반환
     * @param serviceName 서비스 이름
     * @return 상태 요약 정보
     */
    public String getCircuitBreakerSummary(String serviceName) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
            
            return String.format("Circuit Breaker [%s] - State: %s, Failure Rate: %.2f%%, Total: %d, Failed: %d, Success: %d",
                serviceName,
                circuitBreaker.getState(),
                metrics.getFailureRate(),
                metrics.getNumberOfSuccessfulCalls() + circuitBreaker.getMetrics().getNumberOfFailedCalls(),
                metrics.getNumberOfFailedCalls(),
                metrics.getNumberOfSuccessfulCalls());
        } catch (Exception e) {
            return String.format("Circuit Breaker [%s] - Error: %s", serviceName, e.getMessage());
        }
    }
}
