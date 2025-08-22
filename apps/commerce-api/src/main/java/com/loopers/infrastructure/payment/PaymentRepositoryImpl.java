package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    /**
     * 결제정보 저장
     * @param payment
     * @return
     */
    @Override
    public Payment save(Payment payment){
        return paymentJpaRepository.save(payment);
    }

    /**
     * 결제정보 찾기
     * @param transactionKey
     * @return
     */
    @Override
    public Payment findByTransactionKey(String transactionKey){
        return paymentJpaRepository.findByTransactionKey(transactionKey);
    }

    /**
     * 상태별 결제정보 조회
     * @param status
     * @return
     */
    @Override
    public List<Payment> findByStatus(TransactionStatus status){
        return paymentJpaRepository.findByStatus(status);
    }

    /**
     * PG 결제 완료 후 결제 테이블 업데이트
     * @param transactionId
     * @param orderId
     * @param status
     * @param reason
     */
    @Override
    public void updatePayment(String transactionId, String orderId, TransactionStatus status, String reason) {
        paymentJpaRepository.updateByOrderIdAndTransactionKey(transactionId, orderId, status, reason);
    }
}
