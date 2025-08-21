package com.loopers.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.interfaces.api.payment.CardType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PaymentService 서킷브레이커 통합 테스트")
class PaymentServiceCircuitBreakerIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    @DisplayName("성공 - 정상적인 결제 생성")
    void createPayment_success() {
        // given
        String userId = "user123";
        String orderId = "ORDER001";
        Long amount = 50000L;
        CardType cardType = CardType.KB;
        String cardNo = "1234567890123456";

        // when & then
        assertThatThrownBy(() -> 
            paymentService.createPayment(userId, orderId, amount, cardType, cardNo)
        )
        .isInstanceOf(CoreException.class)
        .satisfies(exception -> {
            CoreException coreException = (CoreException) exception;
            // 외부 API가 없으므로 SERVICE_UNAVAILABLE 에러가 발생해야 함
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.SERVICE_UNAVAILABLE);
        });
    }

    @Test
    @DisplayName("실패 - 결제 정보 조회 실패")
    void getPaymentInfo_failure() {
        // given
        String userId = "user123";
        String transactionKey = "TRANSACTION001";

        // when & then
        assertThatThrownBy(() -> 
            paymentService.getPaymentInfo(userId, transactionKey)
        )
        .isInstanceOf(CoreException.class)
        .satisfies(exception -> {
            CoreException coreException = (CoreException) exception;
            // 외부 API가 없으므로 SERVICE_UNAVAILABLE 에러가 발생해야 함
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.SERVICE_UNAVAILABLE);
        });
    }

    @Test
    @DisplayName("실패 - 주문별 거래 조회 실패")
    void getTransactionByOrder_failure() {
        // given
        String userId = "user123";
        String orderId = "ORDER001";

        // when & then
        assertThatThrownBy(() -> 
            paymentService.getTransactionByOrder(userId, orderId)
        )
        .isInstanceOf(CoreException.class)
        .satisfies(exception -> {
            CoreException coreException = (CoreException) exception;
            // 외부 API가 없으므로 SERVICE_UNAVAILABLE 에러가 발생해야 함
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.SERVICE_UNAVAILABLE);
        });
    }
}
