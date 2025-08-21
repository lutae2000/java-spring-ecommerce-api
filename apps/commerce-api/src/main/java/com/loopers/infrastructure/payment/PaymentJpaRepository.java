package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<Payment, String> {

    Payment findByTransactionKey(String transactionKey);
    
    List<Payment> findByStatus(TransactionStatus status);
}
