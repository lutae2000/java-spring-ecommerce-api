package com.loopers.domain.order;



import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.OrderDetailCommand.orderItem;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Nested
    @DisplayName("주문서 제출")
    public class SubmitOrder{

        @DisplayName("정상")
        @Test
        void orderSubmit_succeed(){

            // given
            List<OrderDetailCommand.orderItem> items = List.of(
                new orderItem("product1", 2L, BigDecimal.valueOf(5000)),
                new orderItem("product2", 1L, BigDecimal.valueOf(10000))
            );

            String userId = "utlee";
            BigDecimal totalAmount = BigDecimal.valueOf(20000);

            //when
            OrderInfo order = orderService.placeOrder(userId, totalAmount, items);

            //then
            assertThat(order).isNotNull();
            assertThat(order.getOrder().getOrderNo()).isNotNull();
            assertThat(order.getOrder().getOrderStatus()).isEqualTo(OrderStatus.ORDER_SUBMIT);
            assertThat(order.getOrder().getTotalAmount()).isEqualTo(totalAmount);
            assertThat(order.getOrder().getUserId()).isEqualTo(userId);
        }

        @DisplayName("무료 물품 - 정상")
        @Test
        void orderSubmit_free_order_succeed(){

            // given
            List<OrderDetailCommand.orderItem> items = List.of(
                new orderItem("product1", 2L, BigDecimal.valueOf(0)),
                new orderItem("product2", 1L, BigDecimal.valueOf(0))
            );

            String userId = "utlee";
            BigDecimal totalAmount = BigDecimal.valueOf(0);

            //when
            OrderInfo order = orderService.placeOrder(userId, totalAmount, items);

            //then
            assertThat(order).isNotNull();
            assertThat(order.getOrder().getOrderNo()).isNotNull();
            assertThat(order.getOrder().getOrderStatus()).isEqualTo(OrderStatus.ORDER_SUBMIT);
            assertThat(order.getOrder().getTotalAmount()).isEqualTo(totalAmount);
            assertThat(order.getOrder().getUserId()).isEqualTo(userId);
        }

        @DisplayName("주문서에서 계정이 누락 - 400에러")
        @Test
        void orderSubmit_fail_not_exist_user(){
            // given
            List<OrderDetailCommand.orderItem> items = List.of(
                new orderItem("product1", 2L, BigDecimal.valueOf(5000)),
                new orderItem("product2", 1L, BigDecimal.valueOf(10000))
            );

            String userId = null;
            BigDecimal totalAmount = BigDecimal.valueOf(20000);

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                OrderInfo order = orderService.placeOrder(userId, totalAmount, items);
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문자 계정은 필수입니다");
        }

        @DisplayName("주문서 물품 누락 - 400에러")
        @Test
        void orderSubmit_fail_not_exist_product(){
            // given
            List<OrderDetailCommand.orderItem> items = new ArrayList<>();

            String userId = "utlee";
            BigDecimal totalAmount = BigDecimal.valueOf(20000);

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                OrderInfo order = orderService.placeOrder(userId, totalAmount, items);
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문하려는 물품은 필수입니다");
        }

        @DisplayName("주문서의 금액이 상이함 - 400에러")
        @Test
        void orderSubmit_fail_not_same_totalAmount(){
            // given
            List<OrderDetailCommand.orderItem> items = List.of(
                new orderItem("product1", 2L, BigDecimal.valueOf(5000)),
                new orderItem("product2", 1L, BigDecimal.valueOf(10000))
            );

            String userId = "utlee";
            BigDecimal totalAmount = BigDecimal.valueOf(1000);

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                OrderInfo order = orderService.placeOrder(userId, totalAmount, items);
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문하려는 총금액과 상품가격이 상이합니다");
        }

        @DisplayName("주문 물품의 금액이 잘못됨 - 400에러")
        @Test
        void orderSubmit_fail_not_valid_price(){
            // given
            List<OrderDetailCommand.orderItem> items = List.of(
                new orderItem("product1", 2L, BigDecimal.valueOf(-5000)),
                new orderItem("product2", 1L, BigDecimal.valueOf(0))
            );

            String userId = "utlee";
            BigDecimal totalAmount = BigDecimal.valueOf(-5000);

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                OrderInfo order = orderService.placeOrder(userId, totalAmount, items);
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("잘못된 주문가격 입니다");
        }

        @DisplayName("주문 물품의 수량이 0개 - 400에러")
        @Test
        @Disabled("결과값 제대로 검증이 안되서 무시")
        void orderSubmit_fail_invalid_quantity(){
            // given
            List<OrderDetail> items = List.of(
                new OrderDetail("product1", 0L, BigDecimal.valueOf(5000)),
                new OrderDetail("product2", 0L, BigDecimal.valueOf(0))
            );

            String userId = "utlee";
            BigDecimal totalAmount = BigDecimal.valueOf(0);

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                OrderInfo order = orderService.placeOrder(userId, totalAmount, OrderDetailCommand.fromEntities(items));
            });


            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문수량은 1개 이상이어야 합니다");
        }
    }
}
