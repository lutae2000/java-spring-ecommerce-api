package com.loopers.application.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loopers.domain.card.Card;
import com.loopers.domain.card.CardService;
import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderDetail;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.OrderResponse;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentResponse;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.TransactionDetailResponse;
import com.loopers.domain.payment.TransactionResponse;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.payment.CardType;
import com.loopers.interfaces.api.payment.PaymentDto.CreateCallbackRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentFacade 테스트")
class PaymentFacadeTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @Mock
    private CardService cardService;

    @InjectMocks
    private PaymentFacade paymentFacade;

    private String userId;
    private String orderId;
    private String transactionKey;
    private Long amount;
    private CardType cardType;
    private String cardNo;

    @BeforeEach
    void setUp() {
        userId = "user123";
        orderId = "ORDER001";
        transactionKey = "TRANSACTION001";
        amount = 50000L;
        cardType = CardType.KB;
        cardNo = "1234567890123456";
    }

    @Nested
    @DisplayName("결제 생성")
    class CreatePayment {

        @Test
        @DisplayName("성공 - 카드 정보가 있는 경우")
        void createPayment_success_with_card_info() {
            // given
            PaymentCriteria.CreatePayment criteria = PaymentCriteria.CreatePayment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .cardType(cardType)
                .cardNo(cardNo)
                .build();

            Card card = new Card(userId, "KB카드", cardType, cardNo);
            PaymentInfo paymentInfo = new PaymentInfo(transactionKey, userId, orderId, cardType, cardNo, amount, "http://localhost:8080/api/v1/payments/callback", TransactionStatus.PENDING);

            when(cardService.getCardByUserId(userId)).thenReturn(card);
            when(paymentService.createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo)))
                .thenReturn(paymentInfo);

            // when
            PaymentInfo result = paymentFacade.createPayment(criteria);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getTransactionKey()).isEqualTo(transactionKey),
                () -> assertThat(result.getUserId()).isEqualTo(userId),
                () -> assertThat(result.getOrderId()).isEqualTo(orderId),
                () -> assertThat(result.getCardType()).isEqualTo(cardType),
                () -> assertThat(result.getCardNo()).isEqualTo(cardNo),
                () -> assertThat(result.getAmount()).isEqualTo(amount),
                () -> assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING)
            );

            verify(cardService, times(1)).getCardByUserId(userId);
            verify(paymentService, times(1)).createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo));
        }

        @Test
        @DisplayName("성공 - 카드 정보가 없지만 요청에 카드 정보가 있는 경우")
        void createPayment_success_with_request_card_info() {
            // given
            PaymentCriteria.CreatePayment criteria = PaymentCriteria.CreatePayment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .cardType(cardType)
                .cardNo(cardNo)
                .build();

            PaymentInfo paymentInfo = new PaymentInfo(transactionKey, userId, orderId, cardType, cardNo, amount, "http://localhost:8080/api/v1/payments/callback", TransactionStatus.PENDING);

            when(cardService.getCardByUserId(userId)).thenReturn(null);
            when(paymentService.createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo)))
                .thenReturn(paymentInfo);

            // when
            PaymentInfo result = paymentFacade.createPayment(criteria);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getTransactionKey()).isEqualTo(transactionKey)
            );

            verify(cardService, times(1)).getCardByUserId(userId);
            verify(paymentService, times(1)).createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo));
        }

        @Test
        @DisplayName("성공 - 카드 정보가 없지만 DB에 카드 정보가 있는 경우")
        void createPayment_success_with_db_card_info() {
            // given
            PaymentCriteria.CreatePayment criteria = PaymentCriteria.CreatePayment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .build();

            Card card = new Card(userId, "KB카드", cardType, cardNo);
            PaymentInfo paymentInfo = new PaymentInfo(transactionKey, userId, orderId, cardType, cardNo, amount, "http://localhost:8080/api/v1/payments/callback", TransactionStatus.PENDING);

            when(cardService.getCardByUserId(userId)).thenReturn(card);
            when(paymentService.createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo)))
                .thenReturn(paymentInfo);

            // when
            PaymentInfo result = paymentFacade.createPayment(criteria);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getTransactionKey()).isEqualTo(transactionKey)
            );

            verify(cardService, times(1)).getCardByUserId(userId);
            verify(paymentService, times(1)).createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo));
        }

        @Test
        @DisplayName("실패 - 결제 수단이 없는 경우")
        void createPayment_failure_no_payment_method() {
            // given
            PaymentCriteria.CreatePayment criteria = PaymentCriteria.CreatePayment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .build();

            when(cardService.getCardByUserId(userId)).thenReturn(null);

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> {
                paymentFacade.createPayment(criteria);
            });

            assertAll(
                () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST),
                () -> assertThat(exception.getMessage()).isEqualTo("결제 수단이 없습니다")
            );

            verify(cardService, times(1)).getCardByUserId(userId);
            verify(paymentService, times(0)).createPayment(anyString(), anyString(), any(), any(), anyString());
        }

        @Test
        @DisplayName("실패 - PaymentService에서 예외 발생")
        void createPayment_failure_payment_service_exception() {
            // given
            PaymentCriteria.CreatePayment criteria = PaymentCriteria.CreatePayment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .cardType(cardType)
                .cardNo(cardNo)
                .build();

            Card card = new Card(userId, "KB카드", cardType, cardNo);
            RuntimeException expectedException = new RuntimeException("Payment service error");

            when(cardService.getCardByUserId(userId)).thenReturn(card);
            when(paymentService.createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo)))
                .thenThrow(expectedException);

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                paymentFacade.createPayment(criteria);
            });

            assertThat(exception).isEqualTo(expectedException);

            verify(cardService, times(1)).getCardByUserId(userId);
            verify(paymentService, times(1)).createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo));
        }

        @Test
        @DisplayName("실패 - PaymentService에서 null 반환")
        void createPayment_failure_payment_service_returns_null() {
            // given
            PaymentCriteria.CreatePayment criteria = PaymentCriteria.CreatePayment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .cardType(cardType)
                .cardNo(cardNo)
                .build();

            Card card = new Card(userId, "KB카드", cardType, cardNo);

            when(cardService.getCardByUserId(userId)).thenReturn(card);
            when(paymentService.createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo)))
                .thenReturn(null);

            // when
            PaymentInfo result = paymentFacade.createPayment(criteria);

            // then
            assertThat(result).isNull();

            verify(cardService, times(1)).getCardByUserId(userId);
            verify(paymentService, times(1)).createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo));
        }
    }

    @Nested
    @DisplayName("결제 내역 조회")
    class GetPaymentInfo {

        @Test
        @DisplayName("성공 - 정상적인 결제 내역 조회")
        void getPaymentInfo_success() {
            // given
            PaymentCriteria.GetPaymentInfo criteria = new PaymentCriteria.GetPaymentInfo(userId, transactionKey);
            TransactionDetailResponse expectedResponse = TransactionDetailResponse.builder()
                .transactionKey(transactionKey)
                .orderId(orderId)
                .status(TransactionStatus.SUCCESS)
                .reason("결제 성공")
                .build();

            when(paymentService.getPaymentInfo(userId, transactionKey))
                .thenReturn(expectedResponse);

            // when
            TransactionDetailResponse result = paymentFacade.getPaymentInfo(criteria);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getTransactionKey()).isEqualTo(transactionKey),
                () -> assertThat(result.getOrderId()).isEqualTo(orderId),
                () -> assertThat(result.getStatus()).isEqualTo(TransactionStatus.SUCCESS),
                () -> assertThat(result.getReason()).isEqualTo("결제 성공")
            );

            verify(paymentService, times(1)).getPaymentInfo(userId, transactionKey);
        }

        @Test
        @DisplayName("실패 - PaymentService에서 예외 발생")
        void getPaymentInfo_failure_payment_service_exception() {
            // given
            PaymentCriteria.GetPaymentInfo criteria = new PaymentCriteria.GetPaymentInfo(userId, transactionKey);
            RuntimeException expectedException = new RuntimeException("Payment service error");

            when(paymentService.getPaymentInfo(userId, transactionKey))
                .thenThrow(expectedException);

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                paymentFacade.getPaymentInfo(criteria);
            });

            assertThat(exception).isEqualTo(expectedException);

            verify(paymentService, times(1)).getPaymentInfo(userId, transactionKey);
        }
    }

    @Nested
    @DisplayName("주문번호로 거래번호 조회")
    class GetTransactionByOrder {

        @Test
        @DisplayName("성공 - 정상적인 거래번호 조회")
        void getTransactionByOrder_success() {
            // given
            PaymentCriteria.GetTransactionByOrder criteria = PaymentCriteria.GetTransactionByOrder.builder()
                .userId(userId)
                .orderId(orderId)
                .build();

            OrderResponse expectedResponse = OrderResponse.builder()
                .orderId(orderId)
                .transactions(Arrays.asList(
                    new TransactionResponse(transactionKey, TransactionStatus.SUCCESS, "결제 성공")
                ))
                .build();

            when(paymentService.getTransactionByOrder(userId, orderId))
                .thenReturn(expectedResponse);

            // when
            OrderResponse result = paymentFacade.getTransactionByOrder(criteria);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getOrderId()).isEqualTo(orderId),
                () -> assertThat(result.getTransactions()).hasSize(1),
                () -> assertThat(result.getTransactions().get(0).getTransactionKey()).isEqualTo(transactionKey)
            );

            verify(paymentService, times(1)).getTransactionByOrder(userId, orderId);
        }

        @Test
        @DisplayName("실패 - PaymentService에서 예외 발생")
        void getTransactionByOrder_failure_payment_service_exception() {
            // given
            PaymentCriteria.GetTransactionByOrder criteria = PaymentCriteria.GetTransactionByOrder.builder()
                .userId(userId)
                .orderId(orderId)
                .build();

            RuntimeException expectedException = new RuntimeException("Payment service error");

            when(paymentService.getTransactionByOrder(userId, orderId))
                .thenThrow(expectedException);

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                paymentFacade.getTransactionByOrder(criteria);
            });

            assertThat(exception).isEqualTo(expectedException);

            verify(paymentService, times(1)).getTransactionByOrder(userId, orderId);
        }
    }

    @Nested
    @DisplayName("PG 콜백 처리")
    class UpdatePaymentStatusAndStock {

        @Test
        @DisplayName("성공 - 정상적인 콜백 처리")
        void updatePaymentStatusAndStock_success() {
            // given
            CreateCallbackRequest callbackRequest = CreateCallbackRequest.builder()
                .transactionKey(transactionKey)
                .orderId(orderId)
                .status(TransactionStatus.SUCCESS)
                .reason("결제 성공")
                .build();

            List<OrderDetail> orderDetails = Arrays.asList(
                OrderDetail.CreateOrderDetail("PRODUCT001", 2L, BigDecimal.valueOf(25000)),
                OrderDetail.CreateOrderDetail("PRODUCT002", 1L, BigDecimal.valueOf(25000))
            );

            when(orderService.findOrderDetailByOrderNo(orderId))
                .thenReturn(orderDetails);

            // when
            paymentFacade.updatePaymentStatusAndStock(callbackRequest);

            // then
            verify(paymentService, times(1)).updatePaymentStatus(
                transactionKey, orderId, TransactionStatus.SUCCESS, "결제 성공"
            );
            verify(orderService, times(1)).findOrderDetailByOrderNo(orderId);
            verify(productService, times(1)).updateStock("PRODUCT001", 2L, OrderStatus.ORDER_PAID);
            verify(productService, times(1)).updateStock("PRODUCT002", 1L, OrderStatus.ORDER_PAID);
        }

        @Test
        @DisplayName("성공 - 주문 상세가 없는 경우")
        void updatePaymentStatusAndStock_success_empty_order_details() {
            // given
            CreateCallbackRequest callbackRequest = CreateCallbackRequest.builder()
                .transactionKey(transactionKey)
                .orderId(orderId)
                .status(TransactionStatus.SUCCESS)
                .reason("결제 성공")
                .build();

            when(orderService.findOrderDetailByOrderNo(orderId))
                .thenReturn(Collections.emptyList());

            // when
            paymentFacade.updatePaymentStatusAndStock(callbackRequest);

            // then
            verify(paymentService, times(1)).updatePaymentStatus(
                transactionKey, orderId, TransactionStatus.SUCCESS, "결제 성공"
            );
            verify(orderService, times(1)).findOrderDetailByOrderNo(orderId);
            verify(productService, times(0)).updateStock(anyString(), any(), any());
        }
    }

    @Nested
    @DisplayName("결제 상태 확인")
    class ProcessPaymentStatusCheck {

        @Test
        @DisplayName("성공 - 결제 성공 주문 처리")
        void processPaymentStatusCheck_success_payment_success() {
            // given
            Order order = Order.builder()
                .orderNo(orderId)
                .userId(userId)
                .orderStatus(OrderStatus.ORDER_PLACED)
                .totalAmount(BigDecimal.valueOf(50000))
                .build();
            List<Order> pendingOrders = Arrays.asList(order);

            OrderResponse orderResponse = OrderResponse.builder()
                .orderId(orderId)
                .transactions(Arrays.asList(
                    new TransactionResponse(transactionKey, TransactionStatus.SUCCESS, "결제 성공")
                ))
                .build();

            List<OrderDetail> orderDetails = Arrays.asList(
                OrderDetail.CreateOrderDetail("PRODUCT001", 2L, BigDecimal.valueOf(25000))
            );

            when(orderService.selectOrderNoByOrderStatus(OrderStatus.ORDER_PLACED))
                .thenReturn(pendingOrders);
            when(paymentService.getTransactionByOrder(userId, orderId))
                .thenReturn(orderResponse);
            when(orderService.findOrderDetailByOrderNo(orderId))
                .thenReturn(orderDetails);

            // when
            paymentFacade.processPaymentStatusCheck();

            // then
            verify(orderService, times(1)).selectOrderNoByOrderStatus(OrderStatus.ORDER_PLACED);
            verify(paymentService, times(1)).getTransactionByOrder(userId, orderId);
            verify(paymentService, times(1)).updatePaymentStatus(
                transactionKey, orderId, TransactionStatus.SUCCESS, "결제 성공"
            );
            verify(productService, times(1)).updateStock("PRODUCT001", 2L, OrderStatus.ORDER_PAID);
        }

        @Test
        @DisplayName("성공 - 결제 미완료 주문 처리")
        void processPaymentStatusCheck_success_payment_pending() {
            // given
            Order order = Order.builder()
                .orderNo(orderId)
                .userId(userId)
                .orderStatus(OrderStatus.ORDER_PLACED)
                .totalAmount(BigDecimal.valueOf(50000))
                .build();
            List<Order> pendingOrders = Arrays.asList(order);

            OrderResponse orderResponse = OrderResponse.builder()
                .orderId(orderId)
                .transactions(null)
                .build();

            Card card = new Card(userId, "KB카드", cardType, cardNo);
            PaymentInfo paymentInfo = new PaymentInfo(transactionKey, userId, orderId, cardType, cardNo, amount, "http://localhost:8080/api/v1/payments/callback", TransactionStatus.PENDING);

            when(orderService.selectOrderNoByOrderStatus(OrderStatus.ORDER_PLACED))
                .thenReturn(pendingOrders);
            when(paymentService.getTransactionByOrder(userId, orderId))
                .thenReturn(orderResponse);
            when(cardService.getCardByUserId(userId))
                .thenReturn(card);
            when(paymentService.createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo)))
                .thenReturn(paymentInfo);

            // when
            paymentFacade.processPaymentStatusCheck();

            // then
            verify(orderService, times(1)).selectOrderNoByOrderStatus(OrderStatus.ORDER_PLACED);
            verify(paymentService, times(1)).getTransactionByOrder(userId, orderId);
            verify(cardService, times(1)).getCardByUserId(userId);
            verify(paymentService, times(1)).createPayment(eq(userId), eq(orderId), eq(amount), eq(cardType), eq(cardNo));
        }

        @Test
        @DisplayName("성공 - 빈 주문 목록")
        void processPaymentStatusCheck_success_empty_orders() {
            // given
            when(orderService.selectOrderNoByOrderStatus(OrderStatus.ORDER_PLACED))
                .thenReturn(Collections.emptyList());

            // when
            paymentFacade.processPaymentStatusCheck();

            // then
            verify(orderService, times(1)).selectOrderNoByOrderStatus(OrderStatus.ORDER_PLACED);
            verify(paymentService, times(0)).getTransactionByOrder(anyString(), anyString());
        }

        @Test
        @DisplayName("실패 - OrderService에서 예외 발생")
        void processPaymentStatusCheck_failure_order_service_exception() {
            // given
            RuntimeException expectedException = new RuntimeException("Order service error");

            when(orderService.selectOrderNoByOrderStatus(OrderStatus.ORDER_PLACED))
                .thenThrow(expectedException);

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> {
                paymentFacade.processPaymentStatusCheck();
            });

            assertAll(
                () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR),
                () -> assertThat(exception.getMessage()).isEqualTo("결제 상태 확인 중 오류가 발생했습니다")
            );

            verify(orderService, times(1)).selectOrderNoByOrderStatus(OrderStatus.ORDER_PLACED);
        }

        @Test
        @DisplayName("실패 - 개별 주문 처리 중 예외 발생하지만 전체 프로세스는 계속")
        void processPaymentStatusCheck_failure_individual_order_exception() {
            // given
            Order order1 = Order.builder()
                .orderNo("ORDER001")
                .userId(userId)
                .orderStatus(OrderStatus.ORDER_PLACED)
                .totalAmount(BigDecimal.valueOf(50000))
                .build();
            Order order2 = Order.builder()
                .orderNo("ORDER002")
                .userId(userId)
                .orderStatus(OrderStatus.ORDER_PLACED)
                .totalAmount(BigDecimal.valueOf(30000))
                .build();
            List<Order> pendingOrders = Arrays.asList(order1, order2);

            OrderResponse orderResponse = OrderResponse.builder()
                .orderId("ORDER002")
                .transactions(Arrays.asList(
                    new TransactionResponse("TRANSACTION002", TransactionStatus.SUCCESS, "결제 성공")
                ))
                .build();

            List<OrderDetail> orderDetails = Arrays.asList(
                OrderDetail.CreateOrderDetail("PRODUCT001", 1L, BigDecimal.valueOf(30000))
            );

            when(orderService.selectOrderNoByOrderStatus(OrderStatus.ORDER_PLACED))
                .thenReturn(pendingOrders);
            when(paymentService.getTransactionByOrder(userId, "ORDER001"))
                .thenThrow(new RuntimeException("Individual order error"));
            when(paymentService.getTransactionByOrder(userId, "ORDER002"))
                .thenReturn(orderResponse);
            when(orderService.findOrderDetailByOrderNo("ORDER002"))
                .thenReturn(orderDetails);

            // when
            paymentFacade.processPaymentStatusCheck();

            // then
            verify(orderService, times(1)).selectOrderNoByOrderStatus(OrderStatus.ORDER_PLACED);
            verify(paymentService, times(1)).getTransactionByOrder(userId, "ORDER001");
            verify(paymentService, times(1)).getTransactionByOrder(userId, "ORDER002");
            verify(paymentService, times(1)).updatePaymentStatus(
                "TRANSACTION002", "ORDER002", TransactionStatus.SUCCESS, "결제 성공"
            );
            verify(productService, times(1)).updateStock("PRODUCT001", 1L, OrderStatus.ORDER_PAID);
        }
    }
}
