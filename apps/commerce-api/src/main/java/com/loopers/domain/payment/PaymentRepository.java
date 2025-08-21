package com.loopers.domain.payment;

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
     * 결제정보 업데이트
     * @param transactionId
     * @param status
     */
    void updatePayment(String transactionId, String orderId, TransactionStatus status, String reason);
}
