package com.loopers.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.interfaces.api.payment.PaymentClient;
import com.loopers.interfaces.api.payment.PaymentCreateReq;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 서킷브레이커 테스트")
class PaymentServiceCircuitBreakerTest {

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private PaymentService paymentService;

    private String userId;
    private String orderId;
    private Long amount;
    private CardType cardType;
    private String cardNo;

    @BeforeEach
    void setUp() {
        userId = "user123";
        orderId = "ORDER001";
        amount = 50000L;
        cardType = CardType.KB;
        cardNo = "1234567890123456";

        // CircuitBreaker 모킹
        when(circuitBreakerRegistry.circuitBreaker("paymentService")).thenReturn(circuitBreaker);
        when(circuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
            try {
                return invocation.getArgument(0);
            } catch (Exception e) {
                throw e;
            }
        });
    }

    @Test
    @DisplayName("성공 - 정상적인 결제 생성")
    void createPayment_success() {
        // given
        PaymentCreateReq request = PaymentCreateReq.builder()
            .orderId(orderId)
            .amount(amount)
            .cardType(cardType)
            .cardNo(cardNo)
            .callbackUrl("http://localhost:8080/api/v1/payment/callback")
            .build();

        PaymentResponse paymentResponse = new PaymentResponse("TRANSACTION001", TransactionStatus.PENDING);
        ApiResponse<PaymentResponse> apiResponse = ApiResponse.success(paymentResponse);
        when(paymentClient.createPayment(any(PaymentCreateReq.class), eq(userId)))
            .thenReturn(apiResponse);

        Payment savedPayment = new Payment(
            "TRANSACTION001", userId, orderId, cardType, cardNo, amount,
            "http://localhost:8080/api/v1/payment/callback", TransactionStatus.PENDING, null
        );
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // when
        PaymentInfo result = paymentService.createPayment(userId, orderId, amount, cardType, cardNo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionKey()).isEqualTo("TRANSACTION001");
        verify(paymentClient, times(1)).createPayment(any(PaymentCreateReq.class), eq(userId));
    }

    @Test
    @DisplayName("실패 - 외부 API 실패로 인한 fallback 호출")
    void createPayment_failure_external_api_fallback() {
        // given
        when(paymentClient.createPayment(any(PaymentCreateReq.class), eq(userId)))
            .thenThrow(new RuntimeException("External API failure"));

        // when & then
        assertThatThrownBy(() -> 
            paymentService.createPayment(userId, orderId, amount, cardType, cardNo)
        )
        .isInstanceOf(CoreException.class)
        .satisfies(exception -> {
            CoreException coreException = (CoreException) exception;
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.SERVICE_UNAVAILABLE);
            assertThat(coreException.getMessage()).isEqualTo("Payment service is temporarily unavailable");
        });
    }

    @Test
    @DisplayName("성공 - 정상적인 결제 정보 조회")
    void getPaymentInfo_success() {
        // given
        String transactionKey = "TRANSACTION001";
        TransactionDetailResponse response = new TransactionDetailResponse(
            transactionKey, orderId, cardType, cardNo, amount, TransactionStatus.SUCCESS, "결제 성공"
        );
        ApiResponse<TransactionDetailResponse> apiResponse = ApiResponse.success(response);
        when(paymentClient.getPaymentInfo(transactionKey, userId))
            .thenReturn(apiResponse);

        // when
        TransactionDetailResponse result = paymentService.getPaymentInfo(userId, transactionKey);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionKey()).isEqualTo(transactionKey);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        verify(paymentClient, times(1)).getPaymentInfo(transactionKey, userId);
    }

    @Test
    @DisplayName("실패 - 결제 정보 조회 실패로 인한 fallback 호출")
    void getPaymentInfo_failure_external_api_fallback() {
        // given
        String transactionKey = "TRANSACTION001";
        when(paymentClient.getPaymentInfo(transactionKey, userId))
            .thenThrow(new RuntimeException("External API failure"));

        // when & then
        assertThatThrownBy(() -> 
            paymentService.getPaymentInfo(userId, transactionKey)
        )
        .isInstanceOf(CoreException.class)
        .satisfies(exception -> {
            CoreException coreException = (CoreException) exception;
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.SERVICE_UNAVAILABLE);
            assertThat(coreException.getMessage()).isEqualTo("Payment service is temporarily unavailable");
        });
    }

    @Test
    @DisplayName("성공 - 정상적인 주문별 거래 조회")
    void getTransactionByOrder_success() {
        // given
        OrderResponse response = new OrderResponse(orderId, java.util.List.of());
        ApiResponse<OrderResponse> apiResponse = ApiResponse.success(response);
        when(paymentClient.getTransactionsByOrder(orderId, userId))
            .thenReturn(apiResponse);

        // when
        OrderResponse result = paymentService.getTransactionByOrder(userId, orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        verify(paymentClient, times(1)).getTransactionsByOrder(orderId, userId);
    }

    @Test
    @DisplayName("실패 - 주문별 거래 조회 실패로 인한 fallback 호출")
    void getTransactionByOrder_failure_external_api_fallback() {
        // given
        when(paymentClient.getTransactionsByOrder(orderId, userId))
            .thenThrow(new RuntimeException("External API failure"));

        // when & then
        assertThatThrownBy(() -> 
            paymentService.getTransactionByOrder(userId, orderId)
        )
        .isInstanceOf(CoreException.class)
        .satisfies(exception -> {
            CoreException coreException = (CoreException) exception;
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.SERVICE_UNAVAILABLE);
            assertThat(coreException.getMessage()).isEqualTo("Payment service is temporarily unavailable");
        });
    }
}
