package com.loopers.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Nested
    @DisplayName("주문서 제출")
    public class SubmitOrder {

        @DisplayName("정상")
        @Test
        void orderSubmit_succeed() {

            // given
            List<OrderDetail> items = List.of(
                new OrderDetail("A0003", 2L, BigDecimal.valueOf(100000)),
                new OrderDetail("A0004", 1L, BigDecimal.valueOf(200000))
            );

            String userId = "utlee";

            Order order = Order.createOrder(userId, items, null, null);

            // when
            OrderInfo orderInfo = orderService.placeOrder(userId, order, null);

            // then
            assertThat(orderInfo).isNotNull();
            assertThat(orderInfo.getOrder().getOrderNo()).isNotNull();
        }

        @DisplayName("무료 물품 - 정상")
        @Test
        void orderSubmit_free_order_succeed() {

            // given
            List<OrderDetail> items = List.of(
                new OrderDetail("A0003", 24L, BigDecimal.valueOf(0)),
                new OrderDetail("A0004", 10L, BigDecimal.valueOf(0))
            );

            String userId = "utlee";

            Order order = Order.createOrder(userId, items, null, null);

            // when
            OrderInfo orderInfo = orderService.placeOrder(userId, order, null);

            // then
            assertAll(
                () -> assertThat(orderInfo).isNotNull(),
                () -> assertThat(orderInfo.getOrder().getOrderNo()).isNotNull()
            );
        }

        @DisplayName("주문서에서 계정이 누락 - 400에러")
        @Test
        void orderSubmit_fail_not_exist_user() {
            // given
            List<OrderDetail> items = List.of(
                new OrderDetail("A0003", 24L, BigDecimal.valueOf(0)),
                new OrderDetail("A0004", 10L, BigDecimal.valueOf(0))
            );

            // when
            CoreException response = assertThrows(CoreException.class, () -> {
                Order order = Order.createOrder(null, items, null, null);
                OrderInfo orderInfo = orderService.placeOrder(null, order, null);
            });
            // then
            assertAll(
                () -> assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                () -> assertThat(response.getMessage()).isEqualTo("주문자 계정은 필수입니다")
            );

        }

        @DisplayName("주문서 물품 누락 - 400에러")
        @Test
        void orderSubmit_fail_not_exist_product() {
            // given
            List<OrderDetail> items = List.of();

            String userId = "utlee";

            // when & then - Order 생성자에서 물품 검증
            CoreException response = assertThrows(CoreException.class, () -> {
                Order order = Order.createOrder(userId, items, null, null);
                OrderInfo orderInfo = orderService.placeOrder(userId, order, null);
            });

            // then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문하려는 물품은 필수입니다");
        }

        @DisplayName("주문 물품의 수량이 0개 - 400에러")
        @Test
        void orderSubmit_fail_not_valid_price() {
            // given

            // when
            CoreException response = assertThrows(CoreException.class, () -> {
                new OrderDetail("product1", 0L, BigDecimal.valueOf(5000)); // 수량 0개
            });

            // then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문수량은 1개 이상이어야 합니다");
        }

        @DisplayName("주문 물품의 수량이 잘못됨 - 400에러")
        @Test
        void orderSubmit_fail_invalid_quantity() {
            // given

            // when
            CoreException response = assertThrows(CoreException.class, () -> {
                OrderDetail.CreateOrderDetail("A0003", 0L, BigDecimal.valueOf(1000)); // 음수 가격
            });

            // then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문수량은 1개 이상이어야 합니다");
        }

        @DisplayName("주문 물품의 수량이 음수 - 400에러")
        @Test
        void orderSubmit_fail_negative_quantity() {
            // given
            String productId = "A0003";
            Long invalidQuantity = -1L;  // 음수 수량
            BigDecimal unitPrice = BigDecimal.valueOf(1000);

            // when - OrderDetail 생성자에서 수량 검증
            CoreException response = assertThrows(CoreException.class, () -> {
                new OrderDetail(productId, invalidQuantity, unitPrice);
            });

            // then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문수량은 1개 이상이어야 합니다");
        }

        @DisplayName("쿠폰이 정상 적용되지 않았습니다 - 400에러")
        @Test
        void orderSubmit_fail_discount_amount_mismatch() {
            // given
            List<OrderDetail> items = List.of(
                new OrderDetail("A0003", 2L, BigDecimal.valueOf(1000)),
                new OrderDetail("A0004", 1L, BigDecimal.valueOf(2000))
            );

            String userId = "utlee";
            BigDecimal discountPrice = BigDecimal.valueOf(1000);

            CoreException response = assertThrows(CoreException.class, () -> {
                Order order = Order.createOrder(userId, items, null, discountPrice);
                orderService.placeOrder(userId, order, discountPrice);
            });

            // then
            assertAll(
                () -> assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                () -> assertThat(response.getMessage()).isEqualTo("쿠폰이 정상 적용되지 않았습니다")
            );
        }
    }

    @Nested
    @DisplayName("주문 조회")
    public class FindOrders {

        @DisplayName("성공 - 사용자의 모든 주문 조회")
        @Test
        void findAllOrderByUserId_success() {
            // given
            String userId = "utlee";

            // when
            List<OrderInfo> orderInfos = orderService.findAllOrderByUserId(userId);

            // then
            assertThat(orderInfos).isNotNull();
            // 주문이 있는 경우에만 검증
            if (!orderInfos.isEmpty()) {
                orderInfos.forEach(orderInfo -> {
                    assertThat(orderInfo.getOrder().getUserId()).isEqualTo(userId);
                    assertThat(orderInfo.getOrder().getOrderNo()).isNotNull();
                    assertThat(orderInfo.getOrder().getOrderStatus()).isNotNull();
                });
            }
        }

        @DisplayName("성공 - 주문이 없는 사용자 조회")
        @Test
        void findAllOrderByUserId_empty_success() {
            // given
            String userId = "non_existent_user";

            // when
            List<OrderInfo> orderInfos = orderService.findAllOrderByUserId(userId);

            // then
            assertThat(orderInfos).isNotNull();
            assertThat(orderInfos).isEmpty();
        }

        @DisplayName("성공 - 주문번호로 주문 조회")
        @Test
        void findOrderInfoByOrderNo_success() {
            // given
            String userId = "utlee";
            List<OrderDetail> items = List.of(
                new OrderDetail("A0003", 1L, BigDecimal.valueOf(10000))
            );
            Order order = Order.createOrder(userId, items, null, null);
            OrderInfo createdOrder = orderService.placeOrder(userId, order, null);
            String orderNo = createdOrder.getOrder().getOrderNo();

            // when
            OrderInfo foundOrder = orderService.findOrderInfoByOrderNo(userId, orderNo);

            // then
            assertAll(
                () -> assertThat(foundOrder).isNotNull(),
                () -> assertThat(foundOrder.getOrder().getOrderNo()).isEqualTo(orderNo),
                () -> assertThat(foundOrder.getOrder().getUserId()).isEqualTo(userId),
                () -> assertThat(foundOrder.getOrder().getOrderStatus()).isNotNull()
            );
        }

        @DisplayName("실패 - 존재하지 않는 주문번호로 조회")
        @Test
        void findOrderInfoByOrderNo_not_found() {
            // given
            String userId = "utlee";
            String nonExistentOrderNo = "NON_EXISTENT_ORDER";

            // when & then
            assertThrows(Exception.class, () -> {
                orderService.findOrderInfoByOrderNo(userId, nonExistentOrderNo);
            });
        }

        @DisplayName("실패 - 다른 사용자의 주문번호로 조회")
        @Test
        void findOrderInfoByOrderNo_wrong_user() {
            // given
            String userId = "utlee";
            String otherUserId = "other_user";
            List<OrderDetail> items = List.of(
                new OrderDetail("A0003", 1L, BigDecimal.valueOf(10000))
            );
            Order order = Order.createOrder(userId, items, null, null);
            OrderInfo createdOrder = orderService.placeOrder(userId, order, null);
            String orderNo = createdOrder.getOrder().getOrderNo();

            // when & then
            assertThrows(Exception.class, () -> {
                orderService.findOrderInfoByOrderNo(otherUserId, orderNo);
            });
        }
    }

    @Nested
    @DisplayName("쿠폰 적용 주문")
    public class CouponOrder {

        @DisplayName("성공 - 쿠폰 적용 주문")
        @Test
        void orderSubmit_with_coupon_success() {
            // given
            List<OrderDetail> items = List.of(
                new OrderDetail("A0003", 2L, BigDecimal.valueOf(10000)),
                new OrderDetail("A0004", 1L, BigDecimal.valueOf(20000))
            );
            String userId = "utlee";
            String couponNo = "COUPON001";
            BigDecimal discountPrice = BigDecimal.valueOf(5000);

            Order order = Order.createOrder(userId, items, couponNo, discountPrice);

            // when
            OrderInfo orderInfo = orderService.placeOrder(userId, order, discountPrice);

            // then
            assertAll(
                () -> assertThat(orderInfo).isNotNull(),
                () -> assertThat(orderInfo.getOrder().getOrderNo()).isNotNull(),
                () -> assertThat(orderInfo.getOrder().getCouponNo()).isEqualTo(couponNo),
                () -> assertThat(orderInfo.getOrder().getDiscountAmount()).isEqualTo(discountPrice),
                () -> assertThat(orderInfo.getOrder().getTotalAmount()).isLessThan(
                    items.stream()
                        .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
            );
        }

        @DisplayName("성공 - 할인금액이 총 금액보다 큰 경우")
        @Test
        void orderSubmit_with_high_discount_success() {
            // given
            List<OrderDetail> items = List.of(
                new OrderDetail("A0003", 1L, BigDecimal.valueOf(1000))
            );
            String userId = "utlee";
            String couponNo = "COUPON002";
            BigDecimal discountPrice = BigDecimal.valueOf(2000); // 총 금액보다 큰 할인

            Order order = Order.createOrder(userId, items, couponNo, discountPrice);

            // when
            OrderInfo orderInfo = orderService.placeOrder(userId, order, discountPrice);

            // then
            assertAll(
                () -> assertThat(orderInfo).isNotNull(),
                () -> assertThat(orderInfo.getOrder().getTotalAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO),
                () -> assertThat(orderInfo.getOrder().getDiscountAmount()).isEqualTo(discountPrice)
            );
        }
    }

    @Nested
    @DisplayName("주문 상세 정보")
    public class OrderDetailInfo {

        @DisplayName("성공 - 주문 상세 정보 포함")
        @Test
        void orderSubmit_with_order_details_success() {
            // given
            List<OrderDetail> items = List.of(
                new OrderDetail("A0003", 2L, BigDecimal.valueOf(10000)),
                new OrderDetail("A0004", 1L, BigDecimal.valueOf(20000))
            );
            String userId = "utlee";

            Order order = Order.createOrder(userId, items, null, null);

            // when
            OrderInfo orderInfo = orderService.placeOrder(userId, order, null);

            // then
            assertAll(
                () -> assertThat(orderInfo).isNotNull(),
                () -> assertThat(orderInfo.getOrder().getOrderDetailList()).hasSize(2),
                () -> assertThat(orderInfo.getOrder().getOrderDetailList().get(0).getProductId()).isEqualTo("A0003"),
                () -> assertThat(orderInfo.getOrder().getOrderDetailList().get(0).getQuantity()).isEqualTo(2L),
                () -> assertThat(orderInfo.getOrder().getOrderDetailList().get(1).getProductId()).isEqualTo("A0004"),
                () -> assertThat(orderInfo.getOrder().getOrderDetailList().get(1).getQuantity()).isEqualTo(1L)
            );
        }
    }
}
