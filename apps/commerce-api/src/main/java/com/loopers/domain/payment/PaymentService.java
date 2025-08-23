package com.loopers.domain.payment;

import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.OrderRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.interfaces.api.payment.PaymentClient;
import com.loopers.interfaces.api.payment.PaymentCreateReq;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.utils.StringUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentClient paymentClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * 주문 생성
     * @param userId
     * @param orderId
     * @param amount
     * @param cardType
     * @param cardNo
     * @return
     */
    @Retry(name = "paymentService", fallbackMethod = "createPaymentFallback")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "createPaymentFallback")
    public PaymentInfo createPayment(String userId, String orderId, Long amount, CardType cardType, String cardNo) {

        try{
            String callbackUrl = "http://localhost:8080/api/v1/payment/callback";

            PaymentCreateReq req = PaymentCreateReq.builder()
                .orderId(orderId)
                .amount(amount)
                .cardType(cardType)
                .cardNo(StringUtil.cardNoWithHyphen(cardNo))
                .callbackUrl(callbackUrl)
                .build();

            ApiResponse<PaymentResponse> response = paymentClient.createPayment(req, userId);

            log.info("Payment created successfully: {}", response.data().getTransactionKey());

            Payment payment = new Payment(
                response.data().getTransactionKey(),
                userId,
                orderId,
                cardType,
                cardNo,
                amount,
                callbackUrl,
                response.data().getStatus(),
                null
            );

            Payment savedPayment = paymentRepository.save(payment);
            return PaymentInfo.from(savedPayment);
        }catch (Exception e){
            log.error("Payment create failed: {}", e.getMessage());
            throw e; // 예외를 다시 던져서 retry와 circuit breaker가 동작하도록 함
        }
    }

    /**
     * 결제 생성 실패 시 fallback 메소드
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
            e.getMessage()
        );

        paymentRepository.save(payment);

        // null을 반환하여 호출자가 실패를 인지할 수 있도록 함
        return null;
    }

    /**
     * 거래번호로 결제 내역 조회
     */
    @Retry(name = "paymentService", fallbackMethod = "getPaymentInfoFallback")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getPaymentInfoFallback")
    @Transactional
    public TransactionDetailResponse getPaymentInfo(String userId, String transactionKey){

        if (StringUtils.isEmpty(transactionKey)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "transactionKey 는 null이 될수 없습니다");
        }

        try{
            ApiResponse<TransactionDetailResponse> response = paymentClient.getPaymentInfo(transactionKey, userId);

            //응답이 정상 승인된 경우 Order 테이블 업데이트
            if(response.data().getStatus() == TransactionStatus.SUCCESS){
                orderRepository.updateOrderStatus(response.data().getOrderId(), OrderStatus.ORDER_PAID);
                paymentRepository.updatePayment(transactionKey, response.data().getOrderId(), TransactionStatus.SUCCESS, response.data().getReason());
            }

            return response.data();
        } catch (Exception e){
            log.error("Payment 정보 조회 실패: {}", e.getMessage());
            throw e;
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
    @Retry(name = "paymentService", fallbackMethod = "getTransactionByOrderFallback")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getTransactionByOrderFallback")
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
