package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.TransactionDetailResponse;
import com.loopers.domain.payment.TransactionInfo;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.domain.payment.PaymentService;
import com.loopers.interfaces.api.payment.PaymentClient;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentStatusCheckService {

    private final PaymentRepository paymentRepository;
    private final PaymentClient paymentClient;
    private final PaymentService paymentService;

    /**
     * 5분마다 PENDING 상태의 결제들을 확인하여 상태 업데이트
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    public void checkPendingPayments() {
        log.info("Starting scheduled payment status check...");
        
        try {
            // PENDING 상태의 결제들 조회
            List<Payment> pendingPayments = paymentRepository.findByStatus(TransactionStatus.PENDING);
            
            log.info("Found {} pending payments to check", pendingPayments.size());
            
            for (Payment payment : pendingPayments) {
                checkPaymentStatus(payment);
            }
            
        } catch (Exception e) {
            log.error("Error during scheduled payment status check: {}", e.getMessage(), e);
        }
    }

    /**
     * 개별 결제 상태 확인
     */
    private void checkPaymentStatus(Payment payment) {
        try {
            log.info("Checking payment status for transactionKey: {}", payment.getTransactionKey());
            
            // PG에서 결제 상태 조회
            ApiResponse<TransactionDetailResponse> apiResponse = paymentClient.getPaymentInfo(
                payment.getTransactionKey(), 
                payment.getUserId()
            );
            
            TransactionDetailResponse response = apiResponse.data();
            
            // 상태가 변경된 경우에만 업데이트
            if (response.getStatus() != payment.getStatus()) {
                log.info("Payment status changed from {} to {} for transactionKey: {}", 
                    payment.getStatus(), response.getStatus(), payment.getTransactionKey());
                
                // 콜백 처리
                TransactionInfo transactionInfo = new TransactionInfo(
                    payment.getTransactionKey(),
                    payment.getOrderId(),
                    payment.getAmount(),
                    response.getReason() != null ? response.getReason() : "Status updated via scheduled check",
                    response.getStatus(),
                    payment.getCardType(),
                    payment.getCardNo()
                );
                
                paymentService.callbackForUpdatePayment(transactionInfo);
                
                log.info("Successfully updated payment status for transactionKey: {}", payment.getTransactionKey());
            } else {
                log.debug("Payment status unchanged for transactionKey: {}", payment.getTransactionKey());
            }
            
        } catch (Exception e) {
            log.error("Error checking payment status for transactionKey {}: {}", 
                payment.getTransactionKey(), e.getMessage(), e);
        }
    }

    /**
     * 수동으로 특정 결제 상태 확인
     */
    public void checkPaymentStatusManually(String transactionKey) {
        Payment payment = paymentRepository.findByTransactionKey(transactionKey);
        if (payment != null) {
            checkPaymentStatus(payment);
        } else {
            log.warn("Payment not found for transactionKey: {}", transactionKey);
        }
    }
}
