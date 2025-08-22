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

import com.loopers.domain.order.OrderRepository;
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

    @Mock
    private OrderRepository orderRepository;

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
        @DisplayName("성공 - 정상적인 결제 생성")
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
        @DisplayName("실패 - PaymentClient API 호출 실패 시 fallback 동작")
        void createPayment_failure_api_call_failed_fallback() {
            // given
            when(paymentClient.createPayment(any(PaymentCreateReq.class), eq(userId)))
                .thenThrow(new RuntimeException("API 호출 실패"));
            when(paymentRepository.save(any(Payment.class)))
                .thenReturn(new Payment(null, userId, orderId, cardType, cardNo, amount, null, TransactionStatus.FAIL, "API 호출 실패"));

            // when
            PaymentInfo result = paymentService.createPayment(userId, orderId, amount, cardType, cardNo);

            // then
            assertThat(result).isNull(); // fallback에서 null 반환

            verify(paymentClient, times(1)).createPayment(any(PaymentCreateReq.class), eq(userId));
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("결제 내역 조회")
    class GetPaymentInfo {

        @Test
        @DisplayName("성공 - 거래번호로 결제 내역 조회")
        void getPaymentInfo_success() {
            // given
            TransactionDetailResponse transactionDetail = TransactionDetailResponse.builder()
                .transactionKey(transactionKey)
                .orderId(orderId)
                .status(TransactionStatus.SUCCESS)
                .reason("결제 성공")
                .build();
            ApiResponse<TransactionDetailResponse> apiResponse = ApiResponse.success(transactionDetail);

            when(paymentClient.getPaymentInfo(transactionKey, userId))
                .thenReturn(apiResponse);

            // when
            TransactionDetailResponse result = paymentService.getPaymentInfo(userId, transactionKey);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getTransactionKey()).isEqualTo(transactionKey),
                () -> assertThat(result.getOrderId()).isEqualTo(orderId),
                () -> assertThat(result.getStatus()).isEqualTo(TransactionStatus.SUCCESS)
            );

            verify(paymentClient, times(1)).getPaymentInfo(transactionKey, userId);
            verify(orderRepository, times(1)).updateOrderStatus(orderId, com.loopers.domain.domainEnum.OrderStatus.ORDER_PAID);
            verify(paymentRepository, times(1)).updatePayment(transactionKey, orderId, TransactionStatus.SUCCESS, "결제 성공");
        }

        @Test
        @DisplayName("실패 - transactionKey가 null인 경우")
        void getPaymentInfo_failure_null_transaction_key() {
            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> {
                paymentService.getPaymentInfo(userId, null);
            });

            assertAll(
                () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                () -> assertThat(exception.getMessage()).isEqualTo("transactionKey 는 null이 될수 없습니다")
            );

            verify(paymentClient, times(0)).getPaymentInfo(anyString(), anyString());
        }

        @Test
        @DisplayName("실패 - PaymentClient API 호출 실패 시 fallback 동작")
        void getPaymentInfo_failure_api_call_failed_fallback() {
            // given
            when(paymentClient.getPaymentInfo(transactionKey, userId))
                .thenThrow(new RuntimeException("API 호출 실패"));

            // when
            TransactionDetailResponse result = paymentService.getPaymentInfo(userId, transactionKey);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getTransactionKey()).isEqualTo(transactionKey),
                () -> assertThat(result.getStatus()).isEqualTo(TransactionStatus.FAIL),
                () -> assertThat(result.getReason()).contains("Payment gateway unavailable")
            );

            verify(paymentClient, times(1)).getPaymentInfo(transactionKey, userId);
        }
    }

    @Nested
    @DisplayName("주문번호로 거래번호 조회")
    class GetTransactionByOrder {

        @Test
        @DisplayName("성공 - 주문번호로 거래번호 조회")
        void getTransactionByOrder_success() {
            // given
            OrderResponse orderResponse = OrderResponse.builder()
                .orderId(orderId)
                .build();
            ApiResponse<OrderResponse> apiResponse = ApiResponse.success(orderResponse);

            when(paymentClient.getTransactionsByOrder(orderId, userId))
                .thenReturn(apiResponse);

            // when
            OrderResponse result = paymentService.getTransactionByOrder(userId, orderId);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getOrderId()).isEqualTo(orderId)
            );

            verify(paymentClient, times(1)).getTransactionsByOrder(orderId, userId);
        }

        @Test
        @DisplayName("실패 - orderId가 null인 경우")
        void getTransactionByOrder_failure_null_order_id() {
            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> {
                paymentService.getTransactionByOrder(userId, null);
            });

            assertAll(
                () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                () -> assertThat(exception.getMessage()).isEqualTo("orderId parameter can't be null")
            );

            verify(paymentClient, times(0)).getTransactionsByOrder(anyString(), anyString());
        }

        @Test
        @DisplayName("실패 - PaymentClient API 호출 실패 시 fallback 동작")
        void getTransactionByOrder_failure_api_call_failed_fallback() {
            // given
            when(paymentClient.getTransactionsByOrder(orderId, userId))
                .thenThrow(new RuntimeException("API 호출 실패"));

            // when
            OrderResponse result = paymentService.getTransactionByOrder(userId, orderId);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getOrderId()).isEqualTo(orderId),
                () -> assertThat(result.getTransactions()).isNull()
            );

            verify(paymentClient, times(1)).getTransactionsByOrder(orderId, userId);
        }
    }

    @Nested
    @DisplayName("다양한 카드 타입 테스트")
    class DifferentCardTypes {

        @Test
        @DisplayName("성공 - SAMSUNG 카드로 결제")
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
        @DisplayName("성공 - HYUNDAI 카드로 결제")
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
