package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderResult;
import com.loopers.domain.order.OrderInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.header.CustomHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {
    
    private final OrderFacade orderFacade;

    /**
     * 주문 생성 (결제 없이)
     * @param userId 사용자 ID (헤더)
     * @param request 주문 생성 요청
     * @return 주문 정보
     */
    @PostMapping("")
    public ApiResponse<OrderDto.Response> createOrder(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId,
        @RequestBody OrderDto.CreateRequest request
    ) {
        log.info("Order creation API called - userId: {}, productCount: {}", userId, request.orderDetails().size());

        if (!StringUtils.hasText(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더는 필수입니다");
        }

        // OrderDetailRequest 리스트 변환
        List<OrderCriteria.OrderDetailRequest> orderDetails = request.orderDetails().stream()
            .map(detail -> new OrderCriteria.OrderDetailRequest(
                detail.productId(),
                detail.quantity(),
                detail.unitPrice()
            ))
            .toList();

        OrderCriteria.CreateOrder criteria = new OrderCriteria.CreateOrder(
            userId,
            orderDetails,
            request.couponNo(),
            request.usePoint(),
            request.discountAmount()
        );

        OrderInfo orderInfo = orderFacade.placeOrder(criteria);
        return ApiResponse.success(OrderDto.Response.from(orderInfo));
    }


    /**
     * 기존 주문에 대한 결제 처리
     * @param userId 사용자 ID (헤더)
     * @param orderNo 주문번호
     * @return 결제 성공 여부
     */
    @PostMapping("/{orderNo}/payment")
    public ApiResponse<OrderDto.PaymentResponse> processPayment(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId,
        @PathVariable String orderNo
    ) {
        log.info("Payment processing API called - userId: {}, orderNo: {}", userId, orderNo);

        if (!StringUtils.hasText(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더는 필수입니다");
        }

        if (!StringUtils.hasText(orderNo)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문번호는 필수입니다");
        }

        // 기존 주문 조회
        OrderCriteria.GetOrderByOrderNo getCriteria = new OrderCriteria.GetOrderByOrderNo(userId, orderNo);
        OrderInfo orderInfo = orderFacade.getOrderByOrderNo(getCriteria);

        // 결제 처리를 위한 임시 criteria 생성 (실제로는 별도 API로 받아야 함)
        OrderCriteria.CreateOrder paymentCriteria = new OrderCriteria.CreateOrder(
            userId,
            orderInfo.getOrder().getOrderDetailList().stream()
                .map(detail -> new OrderCriteria.OrderDetailRequest(
                    detail.getProductId(),
                    detail.getQuantity(),
                    detail.getUnitPrice()
                ))
                .toList(),
            orderInfo.getOrder().getCouponNo(),
            BigDecimal.ZERO, // 포인트는 이미 사용됨
            orderInfo.getOrder().getDiscountAmount()
        );

        boolean paymentSuccess = orderFacade.processPaymentAndUpdateStock(paymentCriteria, orderInfo);
        return ApiResponse.success(new OrderDto.PaymentResponse(paymentSuccess));
    }

    /**
     * 사용자의 모든 주문 조회
     * @param userId 사용자 ID (헤더)
     * @return 주문 목록
     */
    @GetMapping("")
    public ApiResponse<List<OrderDto.Response>> getOrders(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId
    ) {
        log.info("Order list retrieval API called - userId: {}", userId);

        if (!StringUtils.hasText(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더는 필수입니다");
        }

        OrderCriteria.GetOrdersByUserId criteria = new OrderCriteria.GetOrdersByUserId(userId);
        List<OrderInfo> orderInfos = orderFacade.getOrdersByUserId(criteria);

        List<OrderDto.Response> responses = orderInfos.stream()
            .map(OrderDto.Response::from)
            .toList();

        return ApiResponse.success(responses);
    }

    /**
     * 주문번호로 주문 상세 조회
     * @param userId 사용자 ID (헤더)
     * @param orderNo 주문번호
     * @return 주문 정보
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<OrderDto.Response> getOrder(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId,
        @PathVariable String orderNo
    ) {
        log.info("Order detail retrieval API called - userId: {}, orderNo: {}", userId, orderNo);

        if (!StringUtils.hasText(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더는 필수입니다");
        }

        if (!StringUtils.hasText(orderNo)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문번호는 필수입니다");
        }

        OrderCriteria.GetOrderByOrderNo criteria = new OrderCriteria.GetOrderByOrderNo(userId, orderNo);
        OrderInfo orderInfo = orderFacade.getOrderByOrderNo(criteria);

        return ApiResponse.success(OrderDto.Response.from(orderInfo));
    }
}
