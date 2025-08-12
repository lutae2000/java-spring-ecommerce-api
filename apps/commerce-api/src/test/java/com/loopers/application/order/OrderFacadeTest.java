package com.loopers.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.application.like.LikeFacade;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.domainEnum.DiscountType;
import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderDetail;
import com.loopers.domain.order.OrderDetailCommand;
import com.loopers.domain.order.OrderDetailCommand.orderItem;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
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

        Product product1 = Product.builder()
            .code("A0001")
            .brandCode("B0001")
            .price(BigDecimal.valueOf(1000))
            .name("테스트 물품")
            .quantity(10L)
            .category1("ELECTRIC")
            .useYn(true)
            .build();
        productRepository.save(product1);

        Product product2 = Product.builder()
            .code("A0002")
            .brandCode("B0001")
            .price(BigDecimal.valueOf(2000))
            .quantity(10L)
            .name("테스트 물품")
            .category1("ELECTRIC")
            .useYn(true)
            .build();
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
    }

    @Nested
    @DisplayName("정상")
    class placeOrder {

        List<OrderDetail> items = List.of(
            new OrderDetail("A0001", 2L, BigDecimal.valueOf(1000)),
            new OrderDetail("A0002", 1L, BigDecimal.valueOf(2000))
        );

        String userId = "utlee";


        @DisplayName("주문 정상")
        @Test
        void order_when_valid_succeed(){

            //given
            Order order = Order.createOrder("user1", items, null,null);
            //when
            OrderResult orderResult = orderFacade.orderSubmit(userId, order);

            //then
            assertAll(
                () -> assertThat(orderResult.orderInfo().getOrder().getOrderNo()).isNotNull()
            );

        }

        @DisplayName("주문 정상 - 쿠폰 적용")
        @Test
        void order_when_valid_succeed_with_coupon(){
            //given
            String couponNo = "1234";
            Order order = Order.createOrder("user1", items, couponNo,null);

            //when
            OrderResult orderResult = orderFacade.orderSubmit(userId, order);

            //then
            assertAll(
                () -> assertThat(orderResult.orderInfo().getOrder().getOrderNo()).isNotNull()
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
            List<OrderDetail> items = List.of(
                new OrderDetail("A0001", 2L, BigDecimal.valueOf(1000)),
                new OrderDetail("A0002", 1L, BigDecimal.valueOf(2000))
            );

            Order order = Order.createOrder("anonymous", items, null,null);

            //when
            CoreException result = Assert.assertThrows(CoreException.class, () -> {
                OrderResult orderResult = orderFacade.orderSubmit("anonymous", order);

            });

            //then
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(result.getMessage()).isEqualTo("존재하는 회원이 없습니다");
        }

        @DisplayName("가지고 있는 포인트보다 총 합계금액이 더 클때 - 400에러")
        @Test
        void order_failed_invalid_totalAmount(){

            //given
            List<OrderDetail> items = List.of(
                new OrderDetail("A0001", 22L, BigDecimal.valueOf(1000)),
                new OrderDetail("A0002", 13L, BigDecimal.valueOf(2000))
            );

            Order order = Order.createOrder(userId, items, null,null);

            //when
            CoreException result = Assert.assertThrows(CoreException.class, () -> {
                OrderResult orderResult = orderFacade.orderSubmit(userId, order);

            });

            //then
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).isEqualTo("가지고 있는 잔액이 부족합니다");
        }

        @DisplayName("미존재 물품으로 주문 - 404에러")
        @Test
        void order_failed_invalid_items(){

            //given
            List<OrderDetail> items = List.of(
                new OrderDetail("A0004", 2L, BigDecimal.valueOf(1000)),
                new OrderDetail("A0005", 1L, BigDecimal.valueOf(2000))
            );

            //when
            CoreException result = Assert.assertThrows(CoreException.class, () -> {
                Order order = Order.createOrder(userId, items, null,null);
                orderFacade.orderSubmit(userId, order);
            });

            //then
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(result.getMessage()).isEqualTo("검색하려는 물품이 없습니다");
        }

        @DisplayName("재고 수량 이상으로 주문  - 400에러")
        @Test
        void order_failed_invalid_items_quantity(){

            //given
            List<OrderDetail> items = List.of(
                new OrderDetail("A0001", 24L, BigDecimal.valueOf(10)),
                new OrderDetail("A0002", 10L, BigDecimal.valueOf(20))
            );

            Order order = Order.createOrder(userId, items, null,null);

            //when
            CoreException result = Assert.assertThrows(CoreException.class, () -> {
                OrderResult orderResult = orderFacade.orderSubmit(userId, order);

            });

            //then
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).isEqualTo("재고가 부족합니다");
        }

        @DisplayName("쿠폰 사용하면서 포인트 부족 - 400에러")
        @Test
        void order_failed_invalid_user_point(){

            String couponNo = "1234";

            //given
            List<OrderDetail> items = List.of(
                new OrderDetail("A0003", 24L, BigDecimal.valueOf(100000)),
                new OrderDetail("A0004", 10L, BigDecimal.valueOf(200000))
            );

            Order order = Order.createOrder(userId, items, couponNo,null);

            //when
            CoreException result = Assert.assertThrows(CoreException.class, () -> {
                OrderResult orderResult = orderFacade.orderSubmit(userId, order);

            });

            //then
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).isEqualTo("가지고 있는 잔액이 부족합니다");
        }
    }
}
