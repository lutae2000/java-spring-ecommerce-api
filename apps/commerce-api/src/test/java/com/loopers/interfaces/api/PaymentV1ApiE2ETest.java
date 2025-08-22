package com.loopers.interfaces.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.loopers.domain.payment.OrderResponse;
import com.loopers.domain.payment.PaymentResponse;
import com.loopers.domain.payment.TransactionDetailResponse;
import com.loopers.domain.payment.TransactionResponse;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.interfaces.api.payment.PaymentClient;
import com.loopers.interfaces.api.payment.PaymentCreateReq;
import com.loopers.interfaces.api.payment.PaymentDto;
import com.loopers.interfaces.api.order.OrderDto;
import com.loopers.interfaces.api.payment.TransactionDetailDto;
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
        // 결제 생성 Mock - 성공 케이스
        PaymentResponse paymentResponse = new PaymentResponse("TRANSACTION001", TransactionStatus.PENDING);
        when(paymentClient.createPayment(any(PaymentCreateReq.class), eq(userId)))
            .thenReturn(ApiResponse.success(paymentResponse));

        // 결제 생성 Mock - 실패 케이스
        when(paymentClient.createPayment(any(PaymentCreateReq.class), eq("user_fail")))
            .thenThrow(new RuntimeException("Payment gateway error"));

        // 거래번호로 조회 Mock - 성공 케이스
        TransactionDetailResponse transactionDetailResponse = TransactionDetailResponse.builder()
            .transactionKey("TRANSACTION001")
            .orderId(orderId)
            .amount(amount)
            .cardType(cardType)
            .cardNo(cardNo)
            .status(TransactionStatus.SUCCESS)
            .reason("결제 성공")
            .build();
        when(paymentClient.getPaymentInfo("TRANSACTION001", userId))
            .thenReturn(ApiResponse.success(transactionDetailResponse));

        // 거래번호로 조회 Mock - 실패 케이스
        when(paymentClient.getPaymentInfo("INVALID_TRANSACTION", userId))
            .thenThrow(new RuntimeException("Transaction not found"));

        // 주문번호로 조회 Mock - 성공 케이스
        OrderResponse orderResponse = OrderResponse.builder()
            .orderId(orderId)
            .transactions(List.of(new TransactionResponse("TRANSACTION001", TransactionStatus.SUCCESS, "정상 승인되었습니다")))
            .build();
        when(paymentClient.getTransactionsByOrder(orderId, userId))
            .thenReturn(ApiResponse.success(orderResponse));

        // 주문번호로 조회 Mock - 실패 케이스
        when(paymentClient.getTransactionsByOrder("INVALID_ORDER", userId))
            .thenThrow(new RuntimeException("Order not found"));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("POST /api/v1/payments - 결제 생성")
    class CreatePayment {

        @Test
        @DisplayName("성공 - 정상적인 결제 생성")
        void createPayment_success() {
            // given
            PaymentDto.CreateRequest request = new PaymentDto.CreateRequest(
                orderId, amount, cardType, cardNo
            );

            // when
            ResponseEntity<ApiResponse<PaymentDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS),
                () -> assertThat(response.getBody().data()).isNotNull(),
                () -> assertThat(response.getBody().data().orderId()).isEqualTo(orderId),
                () -> assertThat(response.getBody().data().cardType()).isEqualTo(cardType),
                () -> assertThat(response.getBody().data().cardNo()).isEqualTo(cardNo),
                () -> assertThat(response.getBody().data().amount()).isEqualTo(amount)
            );
        }

        @Test
        @DisplayName("성공 - SAMSUNG 카드로 결제")
        void createPayment_with_samsung_card_success() {
            // given
            PaymentDto.CreateRequest request = new PaymentDto.CreateRequest(
                orderId, amount, CardType.SAMSUNG, cardNo
            );

            // when
            ResponseEntity<ApiResponse<PaymentDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().cardType()).isEqualTo(CardType.SAMSUNG)
            );
        }

        @Test
        @DisplayName("성공 - HYUNDAI 카드로 결제")
        void createPayment_with_hyundai_card_success() {
            // given
            PaymentDto.CreateRequest request = new PaymentDto.CreateRequest(
                orderId, amount, CardType.HYUNDAI, cardNo
            );

            // when
            ResponseEntity<ApiResponse<PaymentDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().cardType()).isEqualTo(CardType.HYUNDAI)
            );
        }

        @Test
        @DisplayName("실패 - 사용자 ID 헤더 누락")
        void createPayment_failure_missing_user_id_header() {
            // given
            PaymentDto.CreateRequest request = new PaymentDto.CreateRequest(
                orderId, amount, cardType, cardNo
            );
            HttpHeaders emptyHeaders = new HttpHeaders();

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, emptyHeaders),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }

        @Test
        @DisplayName("실패 - PaymentClient API 호출 실패")
        void createPayment_failure_payment_client_error() {
            // given
            PaymentDto.CreateRequest request = new PaymentDto.CreateRequest(
                orderId, amount, cardType, cardNo
            );
            HttpHeaders failHeaders = new HttpHeaders();
            failHeaders.add(CustomHeader.USER_ID, "user_fail");

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, failHeaders),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL),
                () -> assertThat(response.getBody().meta().message()).contains("결제 생성에 실패했습니다")
            );
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 데이터")
        void createPayment_failure_invalid_request_data() {
            // given
            PaymentDto.CreateRequest request = new PaymentDto.CreateRequest(
                null, // orderId가 null
                amount,
                cardType,
                cardNo
            );

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/transaction - 거래번호로 결제 조회")
    class GetPaymentInfoByTransactionKey {

        @Test
        @DisplayName("성공 - 거래번호로 결제 정보 조회")
        void getPaymentInfoByTransactionKey_success() {
            // given
            String transactionKey = "TRANSACTION001";

            // when
            ResponseEntity<ApiResponse<TransactionDetailDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT + "/transaction?transactionKey=" + transactionKey,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS),
                () -> assertThat(response.getBody().data()).isNotNull(),
                () -> assertThat(response.getBody().data().transactionKey()).isEqualTo(transactionKey),
                () -> assertThat(response.getBody().data().orderId()).isEqualTo(orderId),
                () -> assertThat(response.getBody().data().status()).isEqualTo(TransactionStatus.SUCCESS)
            );
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
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }

        @Test
        @DisplayName("실패 - transactionKey 파라미터 누락")
        void getPaymentInfoByTransactionKey_missing_parameter() {
            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT + "/transaction",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 거래번호")
        void getPaymentInfoByTransactionKey_not_found() {
            // given
            String nonExistentTransactionKey = "INVALID_TRANSACTION";

            // when
            ResponseEntity<ApiResponse<TransactionDetailDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT + "/transaction?transactionKey=" + nonExistentTransactionKey,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS),
                () -> assertThat(response.getBody().data().status()).isEqualTo(TransactionStatus.FAIL),
                () -> assertThat(response.getBody().data().reason()).contains("Payment gateway unavailable")
            );
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/order - 주문번호로 거래 조회")
    class GetTransactionByOrder {

        @Test
        @DisplayName("성공 - 주문번호로 거래번호 조회")
        void getTransactionByOrder_success() {
            // when
            ResponseEntity<ApiResponse<OrderDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT + "/order?orderId=" + orderId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS)
            );
        }

        @Test
        @DisplayName("실패 - 사용자 ID 헤더 누락")
        void getTransactionByOrder_missing_user_id_header() {
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
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }

        @Test
        @DisplayName("실패 - orderId 파라미터 누락")
        void getTransactionByOrder_missing_parameter() {
            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT + "/order",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문번호")
        void getTransactionByOrder_not_found() {
            // given
            String nonExistentOrderId = "INVALID_ORDER";

            // when
            ResponseEntity<ApiResponse<OrderDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT + "/order?orderId=" + nonExistentOrderId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS)
            );
        }
    }

    @Nested
    @DisplayName("통합 테스트")
    class IntegrationTest {

        @Test
        @DisplayName("성공 - 결제 생성 후 조회")
        void payment_create_and_retrieve_success() {
            // given - 결제 생성
            PaymentDto.CreateRequest createRequest = new PaymentDto.CreateRequest(
                "ORDER_INTEGRATION", amount, cardType, cardNo
            );

            ResponseEntity<ApiResponse<PaymentDto.Response>> createResponse = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(createRequest, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then - 결제 생성 확인
            assertAll(
                () -> assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(createResponse.getBody().data()).isNotNull()
            );

            // given - 거래번호로 조회
            String transactionKey = createResponse.getBody().data().transactionKey();

            // when - 거래번호로 조회
            ResponseEntity<ApiResponse<TransactionDetailDto.Response>> getResponse = testRestTemplate.exchange(
                ENDPOINT + "/transaction?transactionKey=" + transactionKey,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then - 조회 결과 확인
            assertAll(
                () -> assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(getResponse.getBody().data()).isNotNull(),
                () -> assertThat(getResponse.getBody().data().transactionKey()).isEqualTo(transactionKey)
            );
        }

        @Test
        @DisplayName("실패 - 결제 생성 실패 후 fallback 동작")
        void payment_create_failure_with_fallback() {
            // given - 실패할 사용자로 결제 생성
            PaymentDto.CreateRequest createRequest = new PaymentDto.CreateRequest(
                "ORDER_FAILURE", amount, cardType, cardNo
            );
            HttpHeaders failHeaders = new HttpHeaders();
            failHeaders.add(CustomHeader.USER_ID, "user_fail");

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(createRequest, failHeaders),
                new ParameterizedTypeReference<>() {}
            );

            // then - fallback 동작 확인
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL),
                () -> assertThat(response.getBody().meta().message()).contains("결제 생성에 실패했습니다")
            );
        }
    }
}
