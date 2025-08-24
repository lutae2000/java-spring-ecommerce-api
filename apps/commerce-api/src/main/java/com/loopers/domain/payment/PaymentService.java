package com.loopers.domain.payment;

import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.OrderRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.interfaces.api.payment.PaymentClient;
import com.loopers.interfaces.api.payment.PaymentCreateReq;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.utils.CircuitBreakerUtils;
import com.loopers.support.utils.StringUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentClient paymentClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CircuitBreakerUtils circuitBreakerUtils;

    /**
     * 결제 생성 (메인 메서드)
     * @param userId
     * @param orderId
     * @param amount
     * @param cardType
     * @param cardNo
     * @return
     */
    public PaymentInfo createPayment(String userId, String orderId, Long amount, CardType cardType, String cardNo) {
        log.info("=== Payment Service Called ===");
        log.info("userId: {}, orderId: {}, amount: {}", userId, orderId, amount);

        circuitBreakerUtils.logCircuitBreakerStatus("paymentService");

        // 테스트용: 특정 조건에서 강제로 예외 발생
        if ("FAIL".equals(orderId)) {
            log.error("Forcing RuntimeException for testing circuit breaker");
            throw new RuntimeException("Test failure for circuit breaker");
        }

        try {
            // 1. 외부 API 호출 (CircuitBreaker와 Retry 적용)
            PaymentResponse paymentResponse = callPaymentGateway(userId, orderId, amount, cardType, cardNo);
            
            // 2. DB 저장 (CircuitBreaker와 Retry 미적용)
            Payment savedPayment = savePaymentToDatabase(userId, orderId, amount, cardType, cardNo, paymentResponse);
            
            return PaymentInfo.from(savedPayment);
        } catch (CoreException e) {
            log.error("Payment business error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Payment system error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 외부 결제 게이트웨이 호출 (CircuitBreaker와 Retry 적용)
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "callPaymentGatewayFallback")
    @Retry(name = "paymentService", fallbackMethod = "callPaymentGatewayFallback")
    private PaymentResponse callPaymentGateway(String userId, String orderId, Long amount, CardType cardType, String cardNo) {
        String callbackUrl = "http://localhost:8080/api/v1/payments/callback";

        PaymentCreateReq req = PaymentCreateReq.builder()
            .orderId(orderId)
            .amount(amount)
            .cardType(cardType)
            .cardNo(StringUtil.cardNoWithHyphen(cardNo))
            .callbackUrl(callbackUrl)
            .build();

        ApiResponse<PaymentResponse> response = paymentClient.createPayment(req, userId);
        log.info("Payment created successfully: {}", response.data().getTransactionKey());
        
        return response.data();
    }

    /**
     * 외부 결제 게이트웨이 호출 실패 시 fallback 메소드
     */
    private PaymentResponse callPaymentGatewayFallback(String userId, String orderId, Long amount, CardType cardType, String cardNo, Exception e) {
        log.warn("Payment gateway call failed, using fallback - userId: {}, orderId: {}, error: {}", userId, orderId, e.getMessage());
        
        // 실패한 결제 정보를 DB에 저장
        Payment payment = new Payment(
            null,
            userId,
            orderId,
            cardType,
            cardNo,
            amount,
            null,
            TransactionStatus.FAIL,
            e.getMessage()
        );

        paymentRepository.upsertPayment(payment);
        
        // 실패 상태의 PaymentResponse 반환
        return new PaymentResponse(null, TransactionStatus.FAIL);
    }

    /**
     * DB에 결제 정보 저장 (CircuitBreaker와 Retry 미적용)
     */
    private Payment savePaymentToDatabase(String userId, String orderId, Long amount, CardType cardType, String cardNo, PaymentResponse paymentResponse) {
        String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
        
        Payment payment = new Payment(
            paymentResponse.getTransactionKey(),
            userId,
            orderId,
            cardType,
            cardNo,
            amount,
            callbackUrl,
            paymentResponse.getStatus(),
            null
        );

        return paymentRepository.upsertPayment(payment);
    }

    /**
     * 결제 생성 실패 시 fallback 메소드 (기존 메서드 유지 - 호환성)
     */
    public PaymentInfo createPaymentFallback(String userId, String orderId, Long amount, CardType cardType, String cardNo, Exception e) {
        log.warn("Payment creation failed, using fallback - userId: {}, orderId: {}, error: {}", userId, orderId, e.getMessage());

        // 실패한 결제 정보를 DB에 저장
        Payment payment = new Payment(
            null,
            userId,
            orderId,
            cardType,
            cardNo,
            amount,
            null,
            TransactionStatus.FAIL,
            null
        );

        paymentRepository.upsertPayment(payment);

        // null을 반환하여 호출자가 실패를 인지할 수 있도록 함
        return null;
    }

    /**
     * 거래번호로 결제 내역 조회
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getPaymentInfoFallback")
    @Retry(name = "paymentService", fallbackMethod = "getPaymentInfoFallback")
    public TransactionDetailResponse getPaymentInfo(String userId, String transactionKey){

        if (StringUtils.isEmpty(transactionKey)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "transactionKey 는 null이 될수 없습니다");
        }

        try{
            ApiResponse<TransactionDetailResponse> response = paymentClient.getPaymentInfo(transactionKey, userId);

            //응답이 정상 승인된 경우 Order 테이블 업데이트
            if(response.data().getStatus() == TransactionStatus.SUCCESS){
                updatePaymentStatus(transactionKey, response.data().getOrderId(), TransactionStatus.SUCCESS, response.data().getReason());
            }

            return response.data();
        } catch (Exception e){
            log.error("Payment 정보 조회 실패: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 결제완료 상태로 변경
     * @param transactionKey
     * @param orderId
     * @param status
     * @param reason
     */
    @Transactional
    public void updatePaymentStatus(String transactionKey, String orderId, TransactionStatus status, String reason){

        int orderUpdated = orderRepository.updateOrderStatus(orderId, OrderStatus.ORDER_PAID);
        int paymentUpdated = paymentRepository.updatePayment(transactionKey, orderId, status, reason);

        if (orderUpdated == 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상태 업데이트에 실패했습니다: " + orderId);
        }
        if (paymentUpdated == 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 정보 업데이트에 실패했습니다: " + transactionKey);
        }
    }

    /**
     * 결제 내역 조회 실패 시 fallback 메소드
     */
    public TransactionDetailResponse getPaymentInfoFallback(String userId, String transactionKey, Exception e) {
        log.warn("Payment info retrieval failed, using fallback - userId: {}, transactionKey: {}, error: {}", userId, transactionKey, e.getMessage());

        // 실패 시 기본 응답 반환
        return TransactionDetailResponse.builder()
            .transactionKey(transactionKey)
            .orderId(null)
            .status(TransactionStatus.FAIL)
            .reason("Payment gateway unavailable: " + e.getMessage())
            .build();
    }

    /**
     * 주문번호로 거래번호 조회
     * @param userId
     * @param orderId
     * @return
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getTransactionByOrderFallback")
    @Retry(name = "paymentService", fallbackMethod = "getTransactionByOrderFallback")
    public OrderResponse getTransactionByOrder(String userId, String orderId){
        if (StringUtils.isEmpty(orderId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "orderId parameter can't be null");
        }

        try{
            ApiResponse<OrderResponse> response = paymentClient.getTransactionsByOrder(orderId, userId);
            return response.data();
        } catch (Exception e){
            log.error("Transaction retrieval by order failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 주문번호로 거래번호 조회 실패 시 fallback 메소드
     */
    public OrderResponse getTransactionByOrderFallback(String userId, String orderId, Exception e) {
        log.warn("Transaction retrieval by order failed, using fallback - userId: {}, orderId: {}, error: {}", userId, orderId, e.getMessage());

        // 실패 시 기본 응답 반환
        return OrderResponse.builder()
            .orderId(orderId)
            .transactions(null)
            .build();
    }
}
