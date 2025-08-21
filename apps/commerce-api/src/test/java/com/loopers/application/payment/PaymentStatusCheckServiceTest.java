package com.loopers.application.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.TransactionDetailResponse;
import com.loopers.domain.payment.TransactionInfo;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.interfaces.api.payment.PaymentClient;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentStatusCheckService 테스트")
class PaymentStatusCheckServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentStatusCheckService paymentStatusCheckService;

    private Payment pendingPayment;
    private String transactionKey;
    private String userId;
    private String orderId;

    @BeforeEach
    void setUp() {
        transactionKey = "TRANSACTION001";
        userId = "user123";
        orderId = "ORDER001";

        pendingPayment = new Payment(
            transactionKey, userId, orderId, CardType.KB, "1234567890123456", 50000L,
            "http://localhost:8080/api/v1/payment/callback", TransactionStatus.PENDING, null
        );
    }

    @Test
    @DisplayName("성공 - PENDING 상태의 결제가 SUCCESS로 변경되는 경우")
    void checkPendingPayments_success_status_changed() {
        // given
        List<Payment> pendingPayments = Arrays.asList(pendingPayment);
        when(paymentRepository.findByStatus(TransactionStatus.PENDING))
            .thenReturn(pendingPayments);

        TransactionDetailResponse successResponse = new TransactionDetailResponse(
            transactionKey, orderId, CardType.KB, "1234567890123456", 50000L, 
            TransactionStatus.SUCCESS, "결제 성공"
        );
        ApiResponse<TransactionDetailResponse> apiResponse = ApiResponse.success(successResponse);
        when(paymentClient.getPaymentInfo(transactionKey, userId))
            .thenReturn(apiResponse);

        // when
        paymentStatusCheckService.checkPendingPayments();

        // then
        verify(paymentRepository, times(1)).findByStatus(TransactionStatus.PENDING);
        verify(paymentClient, times(1)).getPaymentInfo(transactionKey, userId);
        verify(paymentService, times(1)).callbackForUpdatePayment(any(TransactionInfo.class));
    }

    @Test
    @DisplayName("성공 - PENDING 상태의 결제가 FAIL로 변경되는 경우")
    void checkPendingPayments_success_status_changed_to_fail() {
        // given
        List<Payment> pendingPayments = Arrays.asList(pendingPayment);
        when(paymentRepository.findByStatus(TransactionStatus.PENDING))
            .thenReturn(pendingPayments);

        TransactionDetailResponse failResponse = new TransactionDetailResponse(
            transactionKey, orderId, CardType.KB, "1234567890123456", 50000L, 
            TransactionStatus.FAIL, "잔액 부족"
        );
        ApiResponse<TransactionDetailResponse> apiResponse = ApiResponse.success(failResponse);
        when(paymentClient.getPaymentInfo(transactionKey, userId))
            .thenReturn(apiResponse);

        // when
        paymentStatusCheckService.checkPendingPayments();

        // then
        verify(paymentRepository, times(1)).findByStatus(TransactionStatus.PENDING);
        verify(paymentClient, times(1)).getPaymentInfo(transactionKey, userId);
        verify(paymentService, times(1)).callbackForUpdatePayment(any(TransactionInfo.class));
    }

    @Test
    @DisplayName("성공 - PENDING 상태가 변경되지 않는 경우")
    void checkPendingPayments_success_status_unchanged() {
        // given
        List<Payment> pendingPayments = Arrays.asList(pendingPayment);
        when(paymentRepository.findByStatus(TransactionStatus.PENDING))
            .thenReturn(pendingPayments);

        TransactionDetailResponse pendingResponse = new TransactionDetailResponse(
            transactionKey, orderId, CardType.KB, "1234567890123456", 50000L, 
            TransactionStatus.PENDING, "처리 중"
        );
        ApiResponse<TransactionDetailResponse> apiResponse = ApiResponse.success(pendingResponse);
        when(paymentClient.getPaymentInfo(transactionKey, userId))
            .thenReturn(apiResponse);

        // when
        paymentStatusCheckService.checkPendingPayments();

        // then
        verify(paymentRepository, times(1)).findByStatus(TransactionStatus.PENDING);
        verify(paymentClient, times(1)).getPaymentInfo(transactionKey, userId);
        verify(paymentService, times(0)).callbackForUpdatePayment(any(TransactionInfo.class));
    }

    @Test
    @DisplayName("성공 - PENDING 상태의 결제가 없는 경우")
    void checkPendingPayments_success_no_pending_payments() {
        // given
        when(paymentRepository.findByStatus(TransactionStatus.PENDING))
            .thenReturn(Arrays.asList());

        // when
        paymentStatusCheckService.checkPendingPayments();

        // then
        verify(paymentRepository, times(1)).findByStatus(TransactionStatus.PENDING);
        verify(paymentClient, times(0)).getPaymentInfo(anyString(), anyString());
        verify(paymentService, times(0)).callbackForUpdatePayment(any(TransactionInfo.class));
    }

    @Test
    @DisplayName("성공 - 수동으로 특정 결제 상태 확인")
    void checkPaymentStatusManually_success() {
        // given
        when(paymentRepository.findByTransactionKey(transactionKey))
            .thenReturn(pendingPayment);

        TransactionDetailResponse successResponse = new TransactionDetailResponse(
            transactionKey, orderId, CardType.KB, "1234567890123456", 50000L, 
            TransactionStatus.SUCCESS, "결제 성공"
        );
        ApiResponse<TransactionDetailResponse> apiResponse = ApiResponse.success(successResponse);
        when(paymentClient.getPaymentInfo(transactionKey, userId))
            .thenReturn(apiResponse);

        // when
        paymentStatusCheckService.checkPaymentStatusManually(transactionKey);

        // then
        verify(paymentRepository, times(1)).findByTransactionKey(transactionKey);
        verify(paymentClient, times(1)).getPaymentInfo(transactionKey, userId);
        verify(paymentService, times(1)).callbackForUpdatePayment(any(TransactionInfo.class));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 결제 수동 확인")
    void checkPaymentStatusManually_failure_payment_not_found() {
        // given
        when(paymentRepository.findByTransactionKey(transactionKey))
            .thenReturn(null);

        // when
        paymentStatusCheckService.checkPaymentStatusManually(transactionKey);

        // then
        verify(paymentRepository, times(1)).findByTransactionKey(transactionKey);
        verify(paymentClient, times(0)).getPaymentInfo(anyString(), anyString());
        verify(paymentService, times(0)).callbackForUpdatePayment(any(TransactionInfo.class));
    }
}
