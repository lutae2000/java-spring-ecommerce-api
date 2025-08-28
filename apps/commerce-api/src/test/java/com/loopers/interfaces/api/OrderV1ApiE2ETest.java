package com.loopers.interfaces.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.loopers.domain.card.Card;
import com.loopers.domain.card.CardService;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.domainEnum.DiscountType;
import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.order.OrderDto;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.support.header.CustomHeader;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderV1ApiE2ETest {

    private static final String ENDPOINT = "/api/v1/orders";
    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PointService pointService;
    private final CouponService couponService;
    private final OrderService orderService;
    private final CardService cardService;

    @Autowired
    public OrderV1ApiE2ETest(
        TestRestTemplate testRestTemplate,
        DatabaseCleanUp databaseCleanUp,
        UserRepository userRepository,
        ProductRepository productRepository,
        PointService pointService,
        CouponService couponService,
        OrderService orderService,
        CardService cardService
    ) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.pointService = pointService;
        this.couponService = couponService;
        this.orderService = orderService;
        this.cardService = cardService;
    }

    private String userId;
    private HttpHeaders headers;
    private Product product1;
    private Product product2;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        userId = "user123";
        
        headers = new HttpHeaders();
        headers.add(CustomHeader.USER_ID, userId);

        // 테스트 데이터 설정
        setupTestData();
    }

    private void setupTestData() {
        // 사용자 생성
        User user = User.builder()
            .userId(userId)
            .email("user123@test.com")
            .birthday("2000-01-01")
            .gender(Gender.M)
            .build();
        userRepository.save(user);

        // 포인트 충전
        PointCommand.Create pointCommand = new PointCommand.Create(userId, 100000L);
        pointService.chargePoint(pointCommand);

        // 상품 생성
        product1 = Product.create(
            "PROD001",
            "테스트 상품1",
            BigDecimal.valueOf(10000),
            100L,
            "image1.jpg",
            "테스트 상품1 설명",
            "ELECTRIC",
            "컴퓨터",
            "노트북"
        );
        productRepository.save(product1);

        product2 = Product.create(
            "PROD002",
            "테스트 상품2",
            BigDecimal.valueOf(20000),
            50L,
            "image2.jpg",
            "테스트 상품2 설명",
            "ELECTRIC",
            "컴퓨터",
            "데스크톱"
        );
        productRepository.save(product2);

        // 쿠폰 생성
        coupon = Coupon.builder()
            .eventId("TEST_EVENT")
            .couponNo("COUPON001")
            .userId(userId)
            .couponName("테스트 쿠폰")
            .useYn(false)
            .discountType(DiscountType.AMOUNT_DISCOUNT)
            .discountAmount(BigDecimal.valueOf(5000))
            .build();
        couponService.save(coupon);

        Card card = new Card("user123", "KB", CardType.KB, "1234567890121234" );
        cardService.saveCard(card);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("POST /api/v1/orders - 주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("성공 - 정상적인 주문 생성")
        void createOrder_success() {
            // given
            OrderDto.CreateRequest request = new OrderDto.CreateRequest(
                List.of(
                    new OrderDto.OrderDetailRequest("PROD001", 2L, BigDecimal.valueOf(10000)),
                    new OrderDto.OrderDetailRequest("PROD002", 1L, BigDecimal.valueOf(20000))
                ),
                null,
                null,
                null
            );

            // when
            ResponseEntity<ApiResponse<OrderDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS),
                () -> assertThat(response.getBody().data()).isNotNull(),
                () -> assertThat(response.getBody().data().userId()).isEqualTo(userId),
                () -> assertThat(response.getBody().data().orderDetails()).hasSize(2),
                () -> assertThat(response.getBody().data().totalAmount()).isEqualTo(BigDecimal.valueOf(40000))
            );
        }

        @Test
        @DisplayName("성공 - 쿠폰 적용 주문 생성")
        void createOrder_with_coupon_success() {
            // given
            OrderDto.CreateRequest request = new OrderDto.CreateRequest(
                List.of(
                    new OrderDto.OrderDetailRequest("PROD001", 1L, BigDecimal.valueOf(10000)),
                    new OrderDto.OrderDetailRequest("PROD002", 1L, BigDecimal.valueOf(20000))
                ),
                "COUPON001",
                null,
                BigDecimal.valueOf(5000)
            );

            // when
            ResponseEntity<ApiResponse<OrderDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().couponNo()).isEqualTo("COUPON001"),
                () -> assertThat(response.getBody().data().discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000)),
                () -> assertThat(response.getBody().data().totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(25000)) // 30000 - 5000
            );
        }

        @Test
        @DisplayName("실패 - 사용자 ID 헤더 누락")
        void createOrder_failure_missing_user_id_header() {
            // given
            OrderDto.CreateRequest request = new OrderDto.CreateRequest(
                List.of(
                    new OrderDto.OrderDetailRequest("PROD001", 1L, BigDecimal.valueOf(10000))
                ),
                null,
                null,
                null
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
        @DisplayName("실패 - 존재하지 않는 상품으로 주문")
        void createOrder_failure_invalid_product() {
            // given
            OrderDto.CreateRequest request = new OrderDto.CreateRequest(
                List.of(
                    new OrderDto.OrderDetailRequest("INVALID_PRODUCT", 1L, BigDecimal.valueOf(10000))
                ),
                null,
                null,
                null
            );

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }

        @Test
        @DisplayName("실패 - 잘못된 주문 상세 데이터")
        void createOrder_failure_invalid_order_details() {
            // given
            OrderDto.CreateRequest request = new OrderDto.CreateRequest(
                List.of(
                    new OrderDto.OrderDetailRequest("PROD001", 0L, BigDecimal.valueOf(10000)) // 수량이 0
                ),
                null,
                null,
                null
            );

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders - 사용자별 주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("성공 - 사용자의 모든 주문 조회")
        void getOrders_success() {
            // given - 주문 생성
            OrderDto.CreateRequest createRequest = new OrderDto.CreateRequest(
                List.of(
                    new OrderDto.OrderDetailRequest("PROD001", 1L, BigDecimal.valueOf(10000))
                ),
                null,
                null,
                null
            );

            testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(createRequest, headers),
                new ParameterizedTypeReference<ApiResponse<OrderDto.Response>>() {}
            );

            // when
            ResponseEntity<ApiResponse<List<OrderDto.Response>>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS),
                () -> assertThat(response.getBody().data()).isNotEmpty(),
                () -> assertThat(response.getBody().data()).hasSize(1),
                () -> assertThat(response.getBody().data().get(0).userId()).isEqualTo(userId)
            );
        }

        @Test
        @DisplayName("성공 - 주문이 없는 사용자 조회")
        void getOrders_empty_success() {
            // given - 다른 사용자로 조회
            HttpHeaders otherUserHeaders = new HttpHeaders();
            otherUserHeaders.add(CustomHeader.USER_ID, "other_user");

            // when
            ResponseEntity<ApiResponse<List<OrderDto.Response>>> response = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(otherUserHeaders),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data()).isEmpty()
            );
        }

        @Test
        @DisplayName("실패 - 사용자 ID 헤더 누락")
        void getOrders_failure_missing_user_id_header() {
            // given
            HttpHeaders emptyHeaders = new HttpHeaders();

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT,
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
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{orderNo} - 주문 상세 조회")
    class GetOrder {

        @Test
        @DisplayName("성공 - 주문 상세 조회")
        void getOrder_success() {
            // given - 주문 생성
            OrderDto.CreateRequest createRequest = new OrderDto.CreateRequest(
                List.of(
                    new OrderDto.OrderDetailRequest("PROD001", 1L, BigDecimal.valueOf(10000))
                ),
                null,
                null,
                null
            );

            ResponseEntity<ApiResponse<OrderDto.Response>> createResponse = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(createRequest, headers),
                new ParameterizedTypeReference<>() {}
            );

            String orderNo = createResponse.getBody().data().orderNo();

            // when
            ResponseEntity<ApiResponse<OrderDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT + "/" + orderNo,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().orderNo()).isEqualTo(orderNo),
                () -> assertThat(response.getBody().data().userId()).isEqualTo(userId),
                () -> assertThat(response.getBody().data().orderDetails()).hasSize(1)
            );
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문번호")
        void getOrder_not_found() {
            // given
            String nonExistentOrderNo = "NON_EXISTENT_ORDER";

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT + "/" + nonExistentOrderNo,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }

        @Test
        @DisplayName("실패 - 사용자 ID 헤더 누락")
        void getOrder_failure_missing_user_id_header() {
            // given
            String orderNo = "ORDER001";
            HttpHeaders emptyHeaders = new HttpHeaders();

            // when
            ResponseEntity<ApiResponse<Object>> response = testRestTemplate.exchange(
                ENDPOINT + "/" + orderNo,
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
    }

    @Nested
    @DisplayName("통합 테스트")
    class IntegrationTest {

        @Test
        @DisplayName("성공 - 주문 생성 후 조회")
        void order_create_and_retrieve_success() {
            // given - 주문 생성
            OrderDto.CreateRequest createRequest = new OrderDto.CreateRequest(
                List.of(
                    new OrderDto.OrderDetailRequest("PROD001", 2L, BigDecimal.valueOf(10000)),
                    new OrderDto.OrderDetailRequest("PROD002", 1L, BigDecimal.valueOf(20000))
                ),
                "COUPON001",
                null,
                BigDecimal.valueOf(5000)
            );

            ResponseEntity<ApiResponse<OrderDto.Response>> createResponse = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(createRequest, headers),
                new ParameterizedTypeReference<>() {}
            );

            // then - 주문 생성 확인
            assertAll(
                () -> assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(createResponse.getBody().data()).isNotNull()
            );

            String orderNo = createResponse.getBody().data().orderNo();

            // when - 주문 상세 조회
            ResponseEntity<ApiResponse<OrderDto.Response>> getResponse = testRestTemplate.exchange(
                ENDPOINT + "/" + orderNo,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
            );

            // then - 조회 결과 확인
            assertAll(
                () -> assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(getResponse.getBody().data().orderNo()).isEqualTo(orderNo),
                () -> assertThat(getResponse.getBody().data().orderDetails()).hasSize(2)
            );

        }
    }
}
