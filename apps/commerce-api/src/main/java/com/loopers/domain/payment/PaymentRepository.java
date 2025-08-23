package com.loopers.domain.payment;

import java.util.List;

public interface PaymentRepository {

    /**
     * 결제정보 저장
     * @param payment
     * @return
     */
    Payment save(Payment payment);

    /**
     * 결제정보 찾기
     * @param transactionKey
     * @return
     */
    Payment findByTransactionKey(String transactionKey);

    /**
     * 상태별 결제정보 조회
     * @param status
     * @return
     */
    List<Payment> findByStatus(TransactionStatus status);

    /**
     * 결제정보 업데이트
     * @param transactionId
     * @param status
     */
    int updatePayment(String transactionId, String orderId, TransactionStatus status, String reason);
}
