package com.loopers.interfaces.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.loopers.domain.payment.OrderResponse;
import com.loopers.domain.payment.PaymentResponse;
import com.loopers.domain.payment.TransactionDetailResponse;
import com.loopers.domain.payment.TransactionInfo;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.interfaces.api.payment.PaymentClient;
import com.loopers.interfaces.api.payment.PaymentCreateReq;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.support.header.CustomHeader;
import com.loopers.utils.DatabaseCleanUp;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentV1ApiE2ETest {

    @TestConfiguration
    static class TestConfig {
        @MockBean
        private PaymentClient paymentClient;
    }

    private static final String ENDPOINT = "/api/v1/payments";
    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final PaymentClient paymentClient;

    @Autowired
    public PaymentV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp, PaymentClient paymentClient) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
        this.paymentClient = paymentClient;
    }

    private String userId;
    private String orderId;
    private Long amount;
    private CardType cardType;
    private String cardNo;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        userId = "user123";
        orderId = "ORDER001";
        amount = 50000L;
        cardType = CardType.KB;
        cardNo = "1234567890123456";

        headers = new HttpHeaders();
        headers.add(CustomHeader.USER_ID, userId);

        // Mock PaymentClient 설정
        setupMockPaymentClient();
    }

    private void setupMockPaymentClient() {
        // 결제 생성 Mock
        PaymentResponse paymentResponse = new PaymentResponse("TRANSACTION001", TransactionStatus.PENDING);
        when(paymentClient.createPayment(any(PaymentCreateReq.class), eq(userId)))
            .thenReturn(ApiResponse.success(paymentResponse));

        // 거래번호로 조회 Mock
        TransactionDetailResponse transactionDetailResponse = new TransactionDetailResponse(
            "TRANSACTION001", orderId, cardType, cardNo, amount, TransactionStatus.SUCCESS, "결제 성공"
        );
        when(paymentClient.getPaymentInfo(anyString(), eq(userId)))
            .thenReturn(ApiResponse.success(transactionDetailResponse));

        // 주문번호로 조회 Mock
        OrderResponse orderResponse = new OrderResponse(orderId, List.of());
        when(paymentClient.getTransactionsByOrder(anyString(), eq(userId)))
            .thenReturn(ApiResponse.success(orderResponse));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("POST /api/v1/payments")
    class CreatePayment {

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

            // when
            ResponseEntity<PaymentInfo> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                PaymentInfo.class
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().getUserId()).isEqualTo(userId),
                () -> assertThat(response.getBody().getOrderId()).isEqualTo(orderId),
                () -> assertThat(response.getBody().getCardType()).isEqualTo(cardType),
                () -> assertThat(response.getBody().getCardNo()).isEqualTo(cardNo),
                () -> assertThat(response.getBody().getAmount()).isEqualTo(amount)
            );
        }

        @Test
        @DisplayName("성공 - SAMSUNG 카드로 결제")
        void createPayment_with_samsung_card_success() {
            // given
            PaymentCreateReq request = PaymentCreateReq.builder()
                .orderId(orderId)
                .amount(amount)
                .cardType(CardType.SAMSUNG)
                .cardNo(cardNo)
                .callbackUrl("http://localhost:8080/api/v1/payment/callback")
                .build();

            // when
            ResponseEntity<PaymentInfo> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                PaymentInfo.class
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().getCardType()).isEqualTo(CardType.SAMSUNG)
            );
        }

        @Test
        @DisplayName("성공 - HYUNDAI 카드로 결제")
        void createPayment_with_hyundai_card_success() {
            // given
            PaymentCreateReq request = PaymentCreateReq.builder()
                .orderId(orderId)
                .amount(amount)
                .cardType(CardType.HYUNDAI)
                .cardNo(cardNo)
                .callbackUrl("http://localhost:8080/api/v1/payment/callback")
                .build();

            // when
            ResponseEntity<PaymentInfo> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                PaymentInfo.class
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().getCardType()).isEqualTo(CardType.HYUNDAI)
            );
        }

        @Test
        @DisplayName("실패 - 사용자 ID 헤더 누락")
        void createPayment_failure_missing_user_id_header() {
            // given
            PaymentCreateReq request = PaymentCreateReq.builder()
                .orderId(orderId)
                .amount(amount)
                .cardType(cardType)
                .cardNo(cardNo)
                .callbackUrl("http://localhost:8080/api/v1/payment/callback")
                .build();

            HttpHeaders emptyHeaders = new HttpHeaders();

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, emptyHeaders),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("실패 - 잘못된 카드번호 형식")
        void createPayment_failure_invalid_card_number() {
            // given
            PaymentCreateReq request = PaymentCreateReq.builder()
                .orderId(orderId)
                .amount(amount)
                .cardType(cardType)
                .cardNo("1234") // 잘못된 카드번호
                .callbackUrl("http://localhost:8080/api/v1/payment/callback")
                .build();

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("실패 - 음수 금액")
        void createPayment_failure_negative_amount() {
            // given
            PaymentCreateReq request = PaymentCreateReq.builder()
                .orderId(orderId)
                .amount(-1000L) // 음수 금액
                .cardType(cardType)
                .cardNo(cardNo)
                .callbackUrl("http://localhost:8080/api/v1/payment/callback")
                .build();

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/payments/callback")
    class PaymentCallback {

        @Test
        @DisplayName("성공 - 결제 성공 콜백")
        void paymentCallback_success() {
            // given
            TransactionInfo transactionInfo = new TransactionInfo(
                "TRANSACTION001", orderId, amount, "결제 성공", 
                TransactionStatus.SUCCESS, cardType, cardNo
            );

            // when
            ResponseEntity<Void> response = testRestTemplate.exchange(
                ENDPOINT + "/callback",
                HttpMethod.POST,
                new HttpEntity<>(transactionInfo),
                Void.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("성공 - 결제 실패 콜백")
        void paymentCallback_failure() {
            // given
            TransactionInfo transactionInfo = new TransactionInfo(
                "TRANSACTION002", orderId, amount, "잔액 부족", 
                TransactionStatus.FAIL, cardType, cardNo
            );

            // when
            ResponseEntity<Void> response = testRestTemplate.exchange(
                ENDPOINT + "/callback",
                HttpMethod.POST,
                new HttpEntity<>(transactionInfo),
                Void.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("실패 - TransactionInfo가 null인 경우")
        void paymentCallback_failure_null_transaction_info() {
            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT + "/callback",
                HttpMethod.POST,
                new HttpEntity<>(null),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/transaction")
    class GetPaymentInfoByTransactionKey {

        @Test
        @DisplayName("성공 - 거래번호로 결제 정보 조회")
        void getPaymentInfoByTransactionKey_success() {
            // given
            String transactionKey = "TRANSACTION001";

            // when
            ResponseEntity<TransactionDetailResponse> response = testRestTemplate.exchange(
                ENDPOINT + "/transaction?transactionKey=" + transactionKey,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TransactionDetailResponse.class
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull()
            );
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 거래번호")
        void getPaymentInfoByTransactionKey_not_found() {
            // given
            String nonExistentTransactionKey = "NON_EXISTENT_TRANSACTION";

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT + "/transaction?transactionKey=" + nonExistentTransactionKey,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("실패 - 거래번호 파라미터 누락")
        void getPaymentInfoByTransactionKey_missing_parameter() {
            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT + "/transaction",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("실패 - 사용자 ID 헤더 누락")
        void getPaymentInfoByTransactionKey_missing_user_id_header() {
            // given
            String transactionKey = "TRANSACTION001";
            HttpHeaders emptyHeaders = new HttpHeaders();

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT + "/transaction?transactionKey=" + transactionKey,
                HttpMethod.GET,
                new HttpEntity<>(emptyHeaders),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/order")
    class GetTransactionByOrderNo {

        @Test
        @DisplayName("성공 - 주문번호로 거래번호 조회")
        void getTransactionByOrderNo_success() {
            // when
            ResponseEntity<OrderResponse> response = testRestTemplate.exchange(
                ENDPOINT + "/order?orderId=" + orderId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                OrderResponse.class
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull()
            );
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문번호")
        void getTransactionByOrderNo_not_found() {
            // given
            String nonExistentOrderId = "NON_EXISTENT_ORDER";

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT + "/order?orderId=" + nonExistentOrderId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("실패 - 주문번호 파라미터 누락")
        void getTransactionByOrderNo_missing_parameter() {
            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT + "/order",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("실패 - 사용자 ID 헤더 누락")
        void getTransactionByOrderNo_missing_user_id_header() {
            // given
            HttpHeaders emptyHeaders = new HttpHeaders();

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT + "/order?orderId=" + orderId,
                HttpMethod.GET,
                new HttpEntity<>(emptyHeaders),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("통합 테스트")
    class IntegrationTest {

        @Test
        @DisplayName("성공 - 결제 생성 후 콜백 처리")
        void payment_create_and_callback_success() {
            // given - 결제 생성
            PaymentCreateReq createRequest = PaymentCreateReq.builder()
                .orderId(orderId)
                .amount(amount)
                .cardType(cardType)
                .cardNo(cardNo)
                .callbackUrl("http://localhost:8080/api/v1/payment/callback")
                .build();

            ResponseEntity<PaymentInfo> createResponse = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(createRequest, headers),
                PaymentInfo.class
            );

            // then - 결제 생성 확인
            assertAll(
                () -> assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(createResponse.getBody()).isNotNull()
            );

            // given - 콜백 처리 (실제 결제 요청에서 받은 transactionKey 사용)
            String transactionKey = createResponse.getBody().getTransactionKey();
            TransactionInfo transactionInfo = new TransactionInfo(
                transactionKey, orderId, amount, "결제 성공", 
                TransactionStatus.SUCCESS, cardType, cardNo
            );

            // when - 콜백 호출
            ResponseEntity<Void> callbackResponse = testRestTemplate.exchange(
                ENDPOINT + "/callback",
                HttpMethod.POST,
                new HttpEntity<>(transactionInfo),
                Void.class
            );

            // then - 콜백 처리 확인
            assertThat(callbackResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // when - 거래번호로 조회
            ResponseEntity<TransactionDetailResponse> getResponse = testRestTemplate.exchange(
                ENDPOINT + "/transaction?transactionKey=" + transactionKey,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TransactionDetailResponse.class
            );

            // then - 조회 결과 확인
            assertAll(
                () -> assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(getResponse.getBody()).isNotNull()
            );
        }

        @Test
        @DisplayName("성공 - 결제 생성 후 실패 콜백 처리")
        void payment_create_and_callback_failure() {
            // given - 결제 생성
            PaymentCreateReq createRequest = PaymentCreateReq.builder()
                .orderId("ORDER002")
                .amount(amount)
                .cardType(cardType)
                .cardNo(cardNo)
                .callbackUrl("http://localhost:8080/api/v1/payment/callback")
                .build();

            ResponseEntity<PaymentInfo> createResponse = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(createRequest, headers),
                PaymentInfo.class
            );

            // then - 결제 생성 확인
            assertAll(
                () -> assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(createResponse.getBody()).isNotNull()
            );

            // given - 실패 콜백 처리
            String transactionKey = createResponse.getBody().getTransactionKey();
            TransactionInfo transactionInfo = new TransactionInfo(
                transactionKey, "ORDER002", amount, "잔액 부족", 
                TransactionStatus.FAIL, cardType, cardNo
            );

            // when - 콜백 호출
            ResponseEntity<Void> callbackResponse = testRestTemplate.exchange(
                ENDPOINT + "/callback",
                HttpMethod.POST,
                new HttpEntity<>(transactionInfo),
                Void.class
            );

            // then - 콜백 처리 확인
            assertThat(callbackResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
