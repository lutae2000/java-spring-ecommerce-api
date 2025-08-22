package com.loopers.infrastructure.payment;

import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PaymentJpaRepository extends JpaRepository<Payment, String> {

    Payment findByTransactionKey(String transactionKey);
    
    List<Payment> findByStatus(TransactionStatus status);

    @Query("update Payment p set p.status = :status, p.reason = :reason where p.orderId = :orderId")
    @Modifying
    void updateByOrderIdAndTransactionKey(String transactionId, String orderId, TransactionStatus status, String reason);
}
