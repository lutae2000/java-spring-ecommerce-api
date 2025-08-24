package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.domainEnum.OrderStatus;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.OrderResponse;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.TransactionDetailResponse;
import com.loopers.domain.payment.TransactionStatus;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.PaymentDto.CreateCallbackRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.header.CustomHeader;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Slf4j
public class PaymentController {

    private final PaymentFacade paymentFacade;

    /**
     * 결제 생성
     * @param userId 사용자 ID (헤더)
     * @param request 결제 생성 요청
     * @return 결제 정보
     */
    @PostMapping("")
    public ApiResponse<Object> createPayment(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId,
        @RequestBody PaymentDto.CreateRequest request
    ) {
        log.info("Payment creation API called - userId: {}, orderId: {}", userId, request.orderId());

        if (!StringUtils.hasText(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더는 필수입니다");
        }

        PaymentCriteria.CreatePayment criteria = new PaymentCriteria.CreatePayment(
            userId,
            request.orderId(),
            request.amount(),
            request.cardType(),
            request.cardNo()
        );

        PaymentInfo paymentInfo = paymentFacade.createPayment(criteria);

        if (paymentInfo == null) {
            return ApiResponse.fail(HttpStatus.BAD_REQUEST.toString(), "결제 생성에 실패했습니다");
        }

        return ApiResponse.success(PaymentDto.Response.from(paymentInfo));
    }

    /**
     * 거래번호로 결제 내역 조회
     * @param userId 사용자 ID (헤더)
     * @param transactionKey 거래번호
     * @return 결제 상세 정보
     */
    @GetMapping("/transaction")
    public ApiResponse<TransactionDetailDto.Response> getPaymentInfo(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId,
        @RequestParam("transactionKey") String transactionKey
    ) {
        log.info("Payment info retrieval API called - userId: {}, transactionKey: {}", userId, transactionKey);

        if (!StringUtils.hasText(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더는 필수입니다");
        }

        if (!StringUtils.hasText(transactionKey)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "transactionKey는 필수입니다");
        }

        PaymentCriteria.GetPaymentInfo criteria = new PaymentCriteria.GetPaymentInfo(userId, transactionKey);
        TransactionDetailResponse response = paymentFacade.getPaymentInfo(criteria);

        return ApiResponse.success(TransactionDetailDto.Response.from(response));
    }

    /**
     * 주문번호로 거래번호 조회
     * @param userId 사용자 ID (헤더)
     * @param orderId 주문번호
     * @return 주문 응답 정보
     */
    @GetMapping("/order")
    public ApiResponse<PaymentOrderDto.Response> getTransactionByOrder(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId,
        @RequestParam("orderId") String orderId
    ) {
        log.info("Transaction retrieval by order API called - userId: {}, orderId: {}", userId, orderId);

        if (!StringUtils.hasText(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더는 필수입니다");
        }

        if (!StringUtils.hasText(orderId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "orderId는 필수입니다");
        }

        PaymentCriteria.GetTransactionByOrder criteria = new PaymentCriteria.GetTransactionByOrder(userId, orderId);
        OrderResponse response = paymentFacade.getTransactionByOrder(criteria);

        return ApiResponse.success(PaymentOrderDto.Response.from(response));
    }

    @PostMapping("/callback")
    public void callbackProcess(
        @RequestBody CreateCallbackRequest createCallbackRequest
    ){
        log.info("Callback API called - createCallbackRequest: {}", createCallbackRequest);
        paymentFacade.updatePaymentStatusAndStock(createCallbackRequest);
    }

    /**
     * 결제 상태 확인 스케줄러 (30초마다 실행)
     * ORDER_PLACED 상태의 주문들의 결제 상태를 확인하고 성공 시 callback 처리
     */
    @Scheduled(fixedDelay = 1000 * 30)
    public void paymentSchedule() {
        log.info("Payment status check scheduler started");
        
        try {
            // Application Layer를 통한 결제 상태 확인 및 callback 처리
            paymentFacade.processPaymentStatusCheck();
            log.info("Payment status check scheduler completed successfully");
        } catch (Exception e) {
            log.error("Payment status check scheduler failed: {}", e.getMessage(), e);
        }
    }
}
