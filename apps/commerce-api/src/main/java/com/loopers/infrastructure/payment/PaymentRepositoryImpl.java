package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
     * 결제정보 업데이트
     * @param transactionId
     * @param status
     */
    @Override
    public void updatePayment(String transactionId, String orderId, TransactionStatus status, String reason){

    }
}
