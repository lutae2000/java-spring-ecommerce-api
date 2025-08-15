package com.loopers.domain.order;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
            orderItem.add(new OrderDetail("A0001", 2L, BigDecimal.valueOf(10000)));

            Order order = Order.createOrder("utlee", orderItem, null, null);
        }


        @DisplayName("주문자 검증 실패 - 400에러")
        @Test
        void order_when_invalid_fail_null_user_id(){
            List<OrderDetail> orderItem = new ArrayList<>();
            orderItem.add(new OrderDetail("A0001", 2L, BigDecimal.valueOf(10000)));

            CoreException response = assertThrows(CoreException.class, () -> {
                Order order = Order.createOrder(null, orderItem, null, null);
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문자 계정은 필수입니다");
        }

        @DisplayName("주문상세가 없는경우 - 400에러")
        @Test
        void order_when_invalid_fail_not_exist_orderItem(){
            List<OrderDetail> orderItem = new ArrayList<>();

            CoreException response = assertThrows(CoreException.class, () -> {
                Order order = Order.createOrder("user", orderItem, null, null);
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("주문하려는 물품은 필수입니다");
        }

        @DisplayName("쿠폰이 정상 적용되지 않았을 경우 - 400에러")
        @Test
        void order_when_invalid_fail_not_same_totalAmount(){
            List<OrderDetail> orderItem = new ArrayList<>();
            orderItem.add(new OrderDetail("A0001", 2L, BigDecimal.valueOf(10000)));

            CoreException response = assertThrows(CoreException.class, () -> {
                Order order = Order.createOrder("user", orderItem, null, BigDecimal.valueOf(100000));
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(response.getMessage()).isEqualTo("쿠폰이 정상 적용되지 않았습니다");
        }
    }
}

