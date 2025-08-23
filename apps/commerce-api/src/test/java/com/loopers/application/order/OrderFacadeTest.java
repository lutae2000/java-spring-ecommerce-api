package com.loopers.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.domain.card.Card;
import com.loopers.domain.card.CardService;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.domainEnum.DiscountType;
import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import java.math.BigDecimal;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderFacadeTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointService pointService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CardService cardService;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void terminateEachTest() {
        databaseCleanUp.truncateAllTables();
    }

    @BeforeEach
    public void setup(){
        User userBuilder = User.builder()
            .userId("utlee")
            .email("utlee@naver.com")
            .birthday("2000-01-01")
            .gender(Gender.M)
            .build();

        userRepository.save(userBuilder);

         PointCommand.Create pointCommand = new PointCommand.Create("utlee", 10000L);
         pointService.chargePoint(pointCommand);

        // Product 생성 방식 수정
        Product product1 = Product.create(
            "A0001",
            "테스트 물품1",
            BigDecimal.valueOf(1000),
            10L,
            "image1.jpg",
            "테스트 물품1 설명",
            "ELECTRIC",
            "컴퓨터",
            "노트북"
        );
        productRepository.save(product1);

        Product product2 = Product.create(
            "A0002",
            "테스트 물품2",
            BigDecimal.valueOf(2000),
            10L,
            "image2.jpg",
            "테스트 물품2 설명",
            "ELECTRIC",
            "컴퓨터",
            "데스크톱"
        );
        productRepository.save(product2);


        Coupon coupon = Coupon.builder()
            .eventId("ABC")
            .couponNo("1234")
            .userId("utlee")
            .couponName("테스트 쿠폰 입니다")
            .useYn(false)
            .discountType(DiscountType.RATIO_DISCOUNT)
            .discountRate(BigDecimal.valueOf(0.1))
            .discountRateLimitPrice(BigDecimal.valueOf(10000))
            .build();

        couponService.save(coupon);

        Card card = new Card("utlee", "KB", CardType.KB, "1234567890121234" );
        cardService.saveCard(card);
    }

    @Nested
    @DisplayName("정상")
    class placeOrder {

        String userId = "utlee";

        @DisplayName("주문 정상")
        @Test
        void order_when_valid_succeed(){
            //given
            List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                new OrderCriteria.OrderDetailRequest("A0001", 2L, BigDecimal.valueOf(1000)),
                new OrderCriteria.OrderDetailRequest("A0002", 1L, BigDecimal.valueOf(2000))
            );

            OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                userId, orderDetails, null, null, null
            );

            //when
            OrderInfo orderInfo = orderFacade.placeOrder(criteria);

            //then
            assertAll(
                () -> assertThat(orderInfo.getOrder().getOrderNo()).isNotNull(),
                () -> assertThat(orderInfo.getOrder().getUserId()).isEqualTo(userId),
                () -> assertThat(orderInfo.getOrder().getOrderDetailList()).hasSize(2)
            );
        }

        @DisplayName("주문 정상 - 쿠폰 적용")
        @Test
        void order_when_valid_succeed_with_coupon(){
            //given
            String couponNo = "1234";
            List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                new OrderCriteria.OrderDetailRequest("A0001", 2L, BigDecimal.valueOf(1000)),
                new OrderCriteria.OrderDetailRequest("A0002", 1L, BigDecimal.valueOf(2000))
            );

            OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                userId, orderDetails, couponNo, null, null
            );

            //when
            OrderInfo orderInfo = orderFacade.placeOrder(criteria);

            //then
            assertAll(
                () -> assertThat(orderInfo.getOrder().getOrderNo()).isNotNull(),
                () -> assertThat(orderInfo.getOrder().getUserId()).isEqualTo(userId),
                () -> assertThat(orderInfo.getOrder().getCouponNo()).isEqualTo(couponNo),
                () -> assertThat(orderInfo.getOrder().getDiscountAmount()).isGreaterThan(BigDecimal.ZERO)
            );
        }

        @DisplayName("주문 및 결제 통합 처리 정상")
        @Test
        void orderWithPayment_when_valid_succeed(){
            //given
            List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                new OrderCriteria.OrderDetailRequest("A0001", 2L, BigDecimal.valueOf(1000)),
                new OrderCriteria.OrderDetailRequest("A0002", 1L, BigDecimal.valueOf(2000))
            );

            OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                userId, orderDetails, null, null, null
            );

            //when
            OrderResult result = orderFacade.placeOrderWithPayment(criteria);

            //then
            assertAll(
                () -> assertThat(result.orderInfo().getOrder().getOrderNo()).isNotNull(),
                () -> assertThat(result.orderInfo().getOrder().getUserId()).isEqualTo(userId),
                () -> assertThat(result.orderInfo().getOrder().getOrderDetailList()).hasSize(2),
                () -> assertThat(result.paymentSuccess()).isTrue(),
                () -> assertThat(result.isSuccess()).isTrue()
            );
        }

        @DisplayName("주문 및 결제 통합 처리 - 쿠폰 적용")
        @Test
        void orderWithPayment_when_valid_succeed_with_coupon(){
            //given
            String couponNo = "1234";
            List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                new OrderCriteria.OrderDetailRequest("A0001", 2L, BigDecimal.valueOf(1000)),
                new OrderCriteria.OrderDetailRequest("A0002", 1L, BigDecimal.valueOf(2000))
            );

            OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                userId, orderDetails, couponNo, null, null
            );

            //when
            OrderResult result = orderFacade.placeOrderWithPayment(criteria);

            //then
            assertAll(
                () -> assertThat(result.orderInfo().getOrder().getOrderNo()).isNotNull(),
                () -> assertThat(result.orderInfo().getOrder().getUserId()).isEqualTo(userId),
                () -> assertThat(result.orderInfo().getOrder().getCouponNo()).isEqualTo(couponNo),
                () -> assertThat(result.orderInfo().getOrder().getDiscountAmount()).isGreaterThan(BigDecimal.ZERO),
                () -> assertThat(result.paymentSuccess()).isTrue(),
                () -> assertThat(result.isSuccess()).isTrue()
            );
        }
    }

    @Nested
    @DisplayName("비정상")
    class placeOrder_invalid {

        String userId = "utlee";

        @DisplayName("주문자 이상 - 404에러")
        @Test
        void order_failed_invalid_userId(){
            //given
            List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                new OrderCriteria.OrderDetailRequest("A0001", 2L, BigDecimal.valueOf(1000)),
                new OrderCriteria.OrderDetailRequest("A0002", 1L, BigDecimal.valueOf(2000))
            );

            OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                "anonymous", orderDetails, null, null,null
            );

            //when & then
            CoreException result = Assert.assertThrows(CoreException.class, () -> {
                orderFacade.placeOrder(criteria);
            });

            //then
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(result.getMessage()).isEqualTo("존재하는 회원이 없습니다");
        }

        @DisplayName("주문 상세가 없는 경우 - 400에러")
        @Test
        void order_failed_empty_order_details(){
            //given
            List<OrderCriteria.OrderDetailRequest> orderDetails = List.of();

            OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
                userId, orderDetails, null, null, null
            );

            //when & then
            CoreException result = Assert.assertThrows(CoreException.class, () -> {
                orderFacade.placeOrder(criteria);
            });

            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).isEqualTo("주문 상품이 비어있습니다");
        }

    }

    @Nested
    @DisplayName("주문 조회")
    class GetOrders {

        String userId = "utlee";

        @DisplayName("사용자별 주문 목록 조회")
        @Test
        void getOrdersByUserId_success() {
            //given - 주문 생성
            List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                new OrderCriteria.OrderDetailRequest("A0001", 1L, BigDecimal.valueOf(1000))
            );

            OrderCriteria.CreateOrder createCriteria = new OrderCriteria.CreateOrder(
                userId, orderDetails, null, null, null
            );
            orderFacade.placeOrder(createCriteria);

            //when
            OrderCriteria.GetOrdersByUserId criteria = new OrderCriteria.GetOrdersByUserId(userId);
            List<OrderInfo> orders = orderFacade.getOrdersByUserId(criteria);

            //then
            assertAll(
                () -> assertThat(orders).isNotEmpty(),
                () -> assertThat(orders).hasSize(1),
                () -> assertThat(orders.get(0).getOrder().getUserId()).isEqualTo(userId)
            );
        }

        @DisplayName("주문번호로 주문 상세 조회")
        @Test
        void getOrderByOrderNo_success() {
            //given - 주문 생성
            List<OrderCriteria.OrderDetailRequest> orderDetails = List.of(
                new OrderCriteria.OrderDetailRequest("A0001", 1L, BigDecimal.valueOf(1000))
            );

            OrderCriteria.CreateOrder createCriteria = new OrderCriteria.CreateOrder(
                userId, orderDetails, null, null, null
            );
            OrderInfo createdOrder = orderFacade.placeOrder(createCriteria);
            String orderNo = createdOrder.getOrder().getOrderNo();

            //when
            OrderCriteria.GetOrderByOrderNo criteria = new OrderCriteria.GetOrderByOrderNo(userId, orderNo);
            OrderInfo orderInfo = orderFacade.getOrderByOrderNo(criteria);

            //then
            assertAll(
                () -> assertThat(orderInfo).isNotNull(),
                () -> assertThat(orderInfo.getOrder().getOrderNo()).isEqualTo(orderNo),
                () -> assertThat(orderInfo.getOrder().getUserId()).isEqualTo(userId)
            );
        }

        @DisplayName("존재하지 않는 주문번호로 조회 - 404에러")
        @Test
        void getOrderByOrderNo_not_found() {
            //given
            String nonExistentOrderNo = "NON_EXISTENT_ORDER";

            //when & then
            OrderCriteria.GetOrderByOrderNo criteria = new OrderCriteria.GetOrderByOrderNo(userId, nonExistentOrderNo);
            CoreException result = Assert.assertThrows(CoreException.class, () -> {
                orderFacade.getOrderByOrderNo(criteria);
            });

            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(result.getMessage()).isEqualTo("주문을 찾을 수 없습니다");
        }
    }
}
