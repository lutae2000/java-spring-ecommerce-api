package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
    public Payment upsertPayment(Payment payment){

        Boolean existsOrderId = paymentJpaRepository.existsByOrderId(payment.getOrderId());

        if(existsOrderId){
            int updated = paymentJpaRepository.updatePayment(payment);

            if(updated == 0){
                throw new CoreException(ErrorType.BAD_REQUEST, "결제내역 업데이트 실패");
            }
            return paymentJpaRepository.findByTransactionKey(payment.getTransactionKey());
        } else {
            return paymentJpaRepository.save(payment);
        }
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
    public int updatePayment(String transactionKey, String orderId, TransactionStatus status, String reason) {
        return paymentJpaRepository.updateByOrderId(transactionKey, orderId, status, reason);
    }
}
