package com.loopers.interfaces.api.payment;

import com.loopers.domain.payment.OrderResponse;
import com.loopers.domain.payment.PaymentResponse;
import com.loopers.domain.payment.TransactionDetailResponse;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.header.CustomHeader;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "payment-gateway",
//    url = "${payment.gateway.url}"
    url = "http://localhost:8082"
)
public interface PaymentClient {

    /**
     * PG에 결제 요청
     * @param request
     * @return
     */
    @PostMapping("/api/v1/payments")
    ApiResponse<PaymentResponse> createPayment(
        @RequestBody PaymentCreateReq request
        , @RequestHeader(value= CustomHeader.USER_ID) String userId
    );

    /**
     * 거래번호로 조회
     */
    @GetMapping("/api/v1/payments/{transactionKey}")
    ApiResponse<TransactionDetailResponse> getPaymentInfo(
        @PathVariable String transactionKey
        , @RequestHeader(value= CustomHeader.USER_ID) String userId
    );

    /**
     * 주문번호로 거래번호 조회
     */
    @GetMapping("/api/v1/payments")
    ApiResponse<OrderResponse> getTransactionsByOrder(
        @RequestParam("orderId") String orderId
        , @RequestHeader(value= CustomHeader.USER_ID) String userId
    );
}
