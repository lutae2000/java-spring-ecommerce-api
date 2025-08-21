package com.loopers.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private String userId;
    private String orderId;
    private Long amount;
    private CardType cardType;
    private String cardNo;
    private String transactionKey;

    @BeforeEach
    void setUp() {
        userId = "user123";
        orderId = "ORDER001";
        amount = 50000L;
        cardType = CardType.KB;
        cardNo = "1234567890123456";
        transactionKey = "TRANSACTION001";
    }

    @Nested
    @DisplayName("결제 생성")
    class CreatePayment {

        @Test
        @DisplayName("성공 - 정상적인 결제 생성 PG가 이상해서 오류남")
        void createPayment_success() {
            // given
            PaymentResponse paymentResponse = new PaymentResponse(transactionKey, TransactionStatus.PENDING);
            ApiResponse<PaymentResponse> apiResponse = ApiResponse.success(paymentResponse);

            Payment savedPayment = new Payment(
                transactionKey, userId, orderId, cardType, cardNo, amount,
                "http://localhost:8080/api/v1/payment/callback", TransactionStatus.PENDING, null
            );

            when(paymentClient.createPayment(any(PaymentCreateReq.class), eq(userId)))
                .thenReturn(apiResponse);
            when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

            // when
            PaymentInfo result = paymentService.createPayment(userId, orderId, amount, cardType, cardNo);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getTransactionKey()).isEqualTo(transactionKey),
                () -> assertThat(result.getUserId()).isEqualTo(userId),
                () -> assertThat(result.getOrderId()).isEqualTo(orderId),
                () -> assertThat(result.getCardType()).isEqualTo(cardType),
                () -> assertThat(result.getCardNo()).isEqualTo(cardNo),
                () -> assertThat(result.getAmount()).isEqualTo(amount),
                () -> assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING)
            );

            verify(paymentClient, times(1)).createPayment(any(PaymentCreateReq.class), eq(userId));
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }

        @Test
        @DisplayName("실패 - PaymentClient API 호출 실패")
        void createPayment_failure_api_call_failed() {
            // given
            when(paymentClient.createPayment(any(PaymentCreateReq.class), eq(userId)))
                .thenThrow(new RuntimeException("API 호출 실패"));

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> {
                paymentService.createPayment(userId, orderId, amount, cardType, cardNo);
            });

            assertAll(
                () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                () -> assertThat(exception.getMessage()).isEqualTo("Payment create failed")
            );

            verify(paymentClient, times(1)).createPayment(any(PaymentCreateReq.class), eq(userId));
            verify(paymentRepository, times(0)).save(any(Payment.class));
        }

        @Test
        @DisplayName("실패 - PaymentClient 예외 발생")
        void createPayment_failure_exception_thrown() {
            // given
            when(paymentClient.createPayment(any(PaymentCreateReq.class), eq(userId)))
                .thenThrow(new RuntimeException("Network error"));

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> {
                paymentService.createPayment(userId, orderId, amount, cardType, cardNo);
            });

            assertAll(
                () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                () -> assertThat(exception.getMessage()).isEqualTo("Payment create failed")
            );

            verify(paymentClient, times(1)).createPayment(any(PaymentCreateReq.class), eq(userId));
            verify(paymentRepository, times(0)).save(any(Payment.class));
        }

        @Test
        @DisplayName("실패 - Repository 저장 실패")
        void createPayment_failure_repository_save_failed() {
            // given
            PaymentResponse paymentResponse = new PaymentResponse(transactionKey, TransactionStatus.PENDING);
            ApiResponse<PaymentResponse> apiResponse = ApiResponse.success(paymentResponse);

            when(paymentClient.createPayment(any(PaymentCreateReq.class), eq(userId)))
                .thenReturn(apiResponse);
            when(paymentRepository.save(any(Payment.class)))
                .thenThrow(new RuntimeException("Database error"));

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> {
                paymentService.createPayment(userId, orderId, amount, cardType, cardNo);
            });

            assertAll(
                () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                () -> assertThat(exception.getMessage()).isEqualTo("Payment create failed")
            );

            verify(paymentClient, times(1)).createPayment(any(PaymentCreateReq.class), eq(userId));
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("결제 상태 업데이트")
    class CallbackForUpdatePayment {

        @Test
        @DisplayName("성공 - 결제 상태 업데이트")
        void callbackForUpdatePayment_success() {
            // given
            TransactionInfo transactionInfo = new TransactionInfo(
                transactionKey, orderId, amount, "결제 성공", TransactionStatus.SUCCESS, cardType, cardNo
            );

            // when
            paymentService.callbackForUpdatePayment(transactionInfo);

            // then
            verify(paymentRepository, times(1))
                .updatePayment(transactionKey, orderId, TransactionStatus.SUCCESS, "결제 성공");
        }

        @Test
        @DisplayName("실패 - TransactionInfo가 null인 경우")
        void callbackForUpdatePayment_failure_null_transaction_info() {
            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> {
                paymentService.callbackForUpdatePayment(null);
            });

            assertAll(
                () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                () -> assertThat(exception.getMessage()).isEqualTo("transactionInfo object can't be null")
            );

            verify(paymentRepository, times(0)).updatePayment(anyString(), anyString(), any(), anyString());
        }

        @Test
        @DisplayName("성공 - 결제 실패 상태 업데이트")
        void callbackForUpdatePayment_success_failed_status() {
            // given
            TransactionInfo transactionInfo = new TransactionInfo(
                transactionKey, orderId, amount, "잔액 부족", TransactionStatus.FAIL, cardType, cardNo
            );

            // when
            paymentService.callbackForUpdatePayment(transactionInfo);

            // then
            verify(paymentRepository, times(1))
                .updatePayment(transactionKey, orderId, TransactionStatus.FAIL, "잔액 부족");
        }
    }

    @Nested
    @DisplayName("다양한 카드 타입 테스트")
    class DifferentCardTypes {

        @Test
        @DisplayName("성공 - SAMSUNG 카드로 결제 - PG가 이상해서 오류남")
        void createPayment_with_samsung_card_success() {
            // given
            CardType samsungCard = CardType.SAMSUNG;
            PaymentResponse paymentResponse = new PaymentResponse(transactionKey, TransactionStatus.PENDING);
            ApiResponse<PaymentResponse> apiResponse = ApiResponse.success(paymentResponse);

            Payment savedPayment = new Payment(
                transactionKey, userId, orderId, samsungCard, cardNo, amount,
                "http://localhost:8080/api/v1/payment/callback", TransactionStatus.PENDING, null
            );

            when(paymentClient.createPayment(any(PaymentCreateReq.class), eq(userId)))
                .thenReturn(apiResponse);
            when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

            // when
            PaymentInfo result = paymentService.createPayment(userId, orderId, amount, samsungCard, cardNo);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getCardType()).isEqualTo(CardType.SAMSUNG)
            );

            verify(paymentClient, times(1)).createPayment(any(PaymentCreateReq.class), eq(userId));
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }

        @Test
        @DisplayName("성공 - HYUNDAI 카드로 결제 - PG가 이상해서 오류남")
        void createPayment_with_hyundai_card_success() {
            // given
            CardType hyundaiCard = CardType.HYUNDAI;
            PaymentResponse paymentResponse = new PaymentResponse(transactionKey, TransactionStatus.PENDING);
            ApiResponse<PaymentResponse> apiResponse = ApiResponse.success(paymentResponse);

            Payment savedPayment = new Payment(
                transactionKey, userId, orderId, hyundaiCard, cardNo, amount,
                "http://localhost:8080/api/v1/payment/callback", TransactionStatus.PENDING, null
            );

            when(paymentClient.createPayment(any(PaymentCreateReq.class), eq(userId)))
                .thenReturn(apiResponse);
            when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

            // when
            PaymentInfo result = paymentService.createPayment(userId, orderId, amount, hyundaiCard, cardNo);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getCardType()).isEqualTo(CardType.HYUNDAI)
            );

            verify(paymentClient, times(1)).createPayment(any(PaymentCreateReq.class), eq(userId));
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }
}
