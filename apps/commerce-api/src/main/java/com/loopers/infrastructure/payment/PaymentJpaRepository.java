package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentJpaRepository extends JpaRepository<Payment, String> {

    Payment save(Payment payment);

    Boolean existsByOrderId(String orderId);

    @Modifying
    @Query(value = """
        UPDATE payment SET 
        transaction_key = :#{#payment.transactionKey},
        card_type = :#{#payment.cardType},
        card_no = :#{#payment.cardNo},
        amount = :#{#payment.amount},
        callback_url = :#{#payment.callbackUrl},
        status = :#{#payment.status},
        reason = :#{#payment.reason},
        updated_at = NOW()
        WHERE order_id = :#{#payment.orderId}
        """, nativeQuery = true)
    int updatePayment(@Param("payment") Payment payment);

    Payment findByTransactionKey(String transactionKey);
    
    List<Payment> findByStatus(TransactionStatus status);

    @Modifying
    @Query("update Payment p set p.status = :status, p.reason = :reason, p.transactionKey = :transactionKey, p.updatedAt = current_timestamp where p.orderId = :orderId")
    int updateByOrderId(@Param("transactionKey") String transactionKey, @Param("orderId") String orderId, @Param("status") TransactionStatus status, @Param("reason") String reason);
}
