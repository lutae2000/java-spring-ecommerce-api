package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderResult;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderDetail;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.domainEnum.OrderStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public class OrderDto {

    /**
     * 주문 생성 요청
     */
    public record CreateRequest(
        List<OrderDetailRequest> orderDetails,
        String couponNo,
        BigDecimal usePoint,
        BigDecimal discountAmount
    ) {}

    /**
     * 주문 상세 요청
     */
    public record OrderDetailRequest(
        String productId,
        Long quantity,
        BigDecimal unitPrice
    ) {}

    /**
     * 주문 응답
     */
    public record Response(
        String orderNo,
        String userId,
        OrderStatus orderStatus,
        String couponNo,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        List<OrderDetailResponse> orderDetails,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
    ) {
        public static Response from(OrderInfo orderInfo) {
            Order order = orderInfo.getOrder();
            
            List<OrderDetailResponse> orderDetailResponses = order.getOrderDetailList().stream()
                .map(OrderDetailResponse::from)
                .toList();

            return new Response(
                order.getOrderNo(),
                order.getUserId(),
                order.getOrderStatus(),
                order.getCouponNo(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                orderDetailResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
            );
        }
    }

    /**
     * 주문 상세 응답
     */
    public record OrderDetailResponse(
        String productId,
        Long quantity,
        BigDecimal unitPrice
    ) {
        public static OrderDetailResponse from(OrderDetail orderDetail) {
            return new OrderDetailResponse(
                orderDetail.getProductId(),
                orderDetail.getQuantity(),
                orderDetail.getUnitPrice()
            );
        }
    }

    /**
     * 주문 결과 응답 (주문 정보 + 결제 성공 여부)
     */
    public record OrderResultResponse(
        String orderNo,
        String userId,
        OrderStatus orderStatus,
        String couponNo,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        List<OrderDetailResponse> orderDetails,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        boolean paymentSuccess
    ) {
        public static OrderResultResponse from(OrderResult orderResult) {
            OrderInfo orderInfo = orderResult.orderInfo();
            Order order = orderInfo.getOrder();
            
            List<OrderDetailResponse> orderDetailResponses = order.getOrderDetailList().stream()
                .map(OrderDetailResponse::from)
                .toList();

            return new OrderResultResponse(
                order.getOrderNo(),
                order.getUserId(),
                order.getOrderStatus(),
                order.getCouponNo(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                orderDetailResponses,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                orderResult.paymentSuccess()
            );
        }
    }

    /**
     * 결제 처리 응답
     */
    public record PaymentResponse(
        boolean paymentSuccess
    ) {}
}
