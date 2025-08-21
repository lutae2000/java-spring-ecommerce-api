package com.loopers.domain.payment;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.interfaces.api.payment.PaymentClient;
import com.loopers.interfaces.api.payment.PaymentCreateReq;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentClient paymentClient;
    private final PaymentRepository paymentRepository;

    /**
     * 주문 생성
     * @param userId
     * @param orderId
     * @param amount
     * @param cardType
     * @param cardNo
     * @return
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "createPaymentFallback")
    @Retry(name = "paymentService")
    public PaymentInfo createPayment(String userId, String orderId, Long amount, CardType cardType, String cardNo) {

        try{
            String callbackUrl = "http://localhost:8080/api/v1/payment/callback";

            PaymentCreateReq req = PaymentCreateReq.builder()
                .orderId(orderId)
                .amount(amount)
                .cardType(cardType)
                .cardNo(cardNo)
                .callbackUrl(callbackUrl)
                .build();

            ApiResponse<PaymentResponse> response = paymentClient.createPayment(req, userId);

            if(response == null || response.meta() == null || response.meta().result() != ApiResponse.Metadata.Result.SUCCESS){
                throw new CoreException(ErrorType.BAD_REQUEST, "Payment create failed");
            }

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
            throw new CoreException(ErrorType.BAD_REQUEST, "Payment create failed");
        }
    }

    /**
     * 거래번호로 결제 내역 조회
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getPaymentInfoFallback")
    @Retry(name = "paymentService")
    public TransactionDetailResponse getPaymentInfo(String userId, String transactionKey){

        if (StringUtils.isEmpty(transactionKey)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "transactionKey parameter can't be null");
        }
        ApiResponse<TransactionDetailResponse> response = paymentClient.getPaymentInfo(transactionKey, userId);
        return response.data();
    }

    /**
     * 주문번호로 거래번호 조회
     * @param userId
     * @param orderId
     * @return
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getTransactionByOrderFallback")
    @Retry(name = "paymentService")
    public OrderResponse getTransactionByOrder(String userId, String orderId){
        if (StringUtils.isEmpty(orderId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "orderId parameter can't be null");
        }
        ApiResponse<OrderResponse> response = paymentClient.getTransactionsByOrder(orderId, userId);
        return response.data();
    }

    /**
     * callback 후 결제내역 업데이트
     */
    public void callbackForUpdatePayment(TransactionInfo transactionInfo){

        if(ObjectUtils.isEmpty(transactionInfo) ){
            throw new CoreException(ErrorType.BAD_REQUEST, "transactionInfo object can't be null");
        }
        paymentRepository.updatePayment(transactionInfo.getTransactionKey(), transactionInfo.getOrderId(), transactionInfo.getStatus(), transactionInfo.getReason());
    }

    /**
     * createPayment fallback 메서드
     */
    public PaymentInfo createPaymentFallback(String userId, String orderId, Long amount, CardType cardType, String cardNo, Exception e) {
        log.warn("Payment creation failed, using fallback. Error: {}", e.getMessage());
        throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "Payment service is temporarily unavailable");
    }

    /**
     * getPaymentInfo fallback 메서드
     */
    public TransactionDetailResponse getPaymentInfoFallback(String userId, String transactionKey, Exception e) {
        log.warn("Payment info retrieval failed, using fallback. Error: {}", e.getMessage());
        throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "Payment service is temporarily unavailable");
    }

    /**
     * getTransactionByOrder fallback 메서드
     */
    public OrderResponse getTransactionByOrderFallback(String userId, String orderId, Exception e) {
        log.warn("Transaction retrieval failed, using fallback. Error: {}", e.getMessage());
        throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "Payment service is temporarily unavailable");
    }
}
