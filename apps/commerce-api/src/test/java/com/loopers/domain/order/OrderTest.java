package com.loopers.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


public class OrderTest {

    @Nested
    @DisplayName("주문")
    class placeOrder {

        @DisplayName("성공")
        @Test
        void order_when_valid_succeed(){

            List<OrderDetail> orderItem = new ArrayList<>();
            orderItem.add(OrderDetail.CreateOrderDetail("A0001", 2L, BigDecimal.valueOf(10000)));

            Order order = Order.createOrder("utlee", orderItem, null, null);
            
            // 검증
            assertThat(order).isNotNull();
            assertThat(order.getUserId()).isEqualTo("utlee");
            assertThat(order.getOrderDetailList()).hasSize(1);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ORDER_PLACED);
            assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.valueOf(20000)); // 2개 * 10000
        }

        @DisplayName("성공 - 쿠폰 적용")
        @Test
        void order_when_valid_succeed_with_coupon(){

            List<OrderDetail> orderItem = new ArrayList<>();
            orderItem.add(OrderDetail.CreateOrderDetail("A0001", 2L, BigDecimal.valueOf(10000)));

            String couponNo = "COUPON001";
            BigDecimal discountAmount = BigDecimal.valueOf(5000);
            
            Order order = Order.createOrder("utlee", orderItem, couponNo, discountAmount);
            
            // 검증
            assertThat(order).isNotNull();
            assertThat(order.getUserId()).isEqualTo("utlee");
            assertThat(order.getCouponNo()).isEqualTo(couponNo);
            assertThat(order.getDiscountAmount()).isEqualTo(discountAmount);
            assertThat(order.getOrderDetailList()).hasSize(1);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ORDER_PLACED);
        }

        @DisplayName("주문자 검증 실패 - 400에러")
        @Test
        void order_when_invalid_fail_null_user_id(){
            List<OrderDetail> orderItem = new ArrayList<>();
            orderItem.add(OrderDetail.CreateOrderDetail("A0001", 2L, BigDecimal.valueOf(10000)));

            CoreException response = assertThrows(CoreException.class, () -> {
                Order.createOrder(null, orderItem, null, null);
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문자 계정은 필수입니다");
        }

        @DisplayName("주문상세가 없는경우 - 400에러")
        @Test
        void order_when_invalid_fail_not_exist_orderItem(){
            List<OrderDetail> orderItem = new ArrayList<>();

            CoreException response = assertThrows(CoreException.class, () -> {
                Order.createOrder("user", orderItem, null, null);
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문하려는 물품은 필수입니다");
        }

        @DisplayName("할인금액이 음수인 경우 - 400에러")
        @Test
        void order_when_invalid_fail_negative_discount_amount(){
            List<OrderDetail> orderItem = new ArrayList<>();
            orderItem.add(OrderDetail.CreateOrderDetail("A0001", 2L, BigDecimal.valueOf(10000)));

            CoreException response = assertThrows(CoreException.class, () -> {
                Order.createOrder("user", orderItem, null, BigDecimal.valueOf(-1000));
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("할인금액은 마이너스일 수 없습니다");
        }

        @DisplayName("성공 - 할인금액이 총금액보다 큰 경우 (총금액 적용)")
        @Test
        void order_when_valid_succeed_high_discount(){
            List<OrderDetail> orderItem = new ArrayList<>();
            orderItem.add(OrderDetail.CreateOrderDetail("A0001", 1L, BigDecimal.valueOf(1000)));

            BigDecimal highDiscountAmount = BigDecimal.valueOf(2000); // 총금액보다 큰 할인
            
            Order order = Order.createOrder("user", orderItem, "COUPON001", highDiscountAmount);
            
            // 검증 - 할인 후 총금액이 원래 총금액과 같아야 함 (할인이 총금액보다 클 때)
            assertThat(order).isNotNull();
            assertThat(order.getDiscountAmount()).isEqualTo(highDiscountAmount);
            assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.valueOf(1000)); // 원래 총금액이 최소값
        }

        @DisplayName("성공 - 무료 상품 주문")
        @Test
        void order_when_valid_succeed_free_product(){
            List<OrderDetail> orderItem = new ArrayList<>();
            orderItem.add(OrderDetail.CreateOrderDetail("A0001", 2L, BigDecimal.valueOf(0))); // 무료 상품

            Order order = Order.createOrder("user", orderItem, null, null);
            
            // 검증
            assertThat(order).isNotNull();
            assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
            assertThat(order.getDiscountAmount()).isEqualTo(BigDecimal.ZERO);
        }

        @DisplayName("성공 - 여러 상품 주문")
        @Test
        void order_when_valid_succeed_multiple_products(){
            List<OrderDetail> orderItem = new ArrayList<>();
            orderItem.add(OrderDetail.CreateOrderDetail("A0001", 2L, BigDecimal.valueOf(10000)));
            orderItem.add(OrderDetail.CreateOrderDetail("A0002", 1L, BigDecimal.valueOf(5000)));

            Order order = Order.createOrder("user", orderItem, null, null);
            
            // 검증
            assertThat(order).isNotNull();
            assertThat(order.getOrderDetailList()).hasSize(2);
            assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.valueOf(25000)); // (2*10000) + (1*5000)
        }
    }
}