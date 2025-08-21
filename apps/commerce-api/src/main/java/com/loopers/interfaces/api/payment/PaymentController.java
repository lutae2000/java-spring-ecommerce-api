package com.loopers.interfaces.api.payment;

import com.loopers.domain.payment.OrderResponse;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.TransactionDetailResponse;
import com.loopers.domain.payment.TransactionInfo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.header.CustomHeader;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService paymentService;

    /**
     * 주문 생성
     */
    @PostMapping("/payments")
    public PaymentInfo paymentCreate(@RequestHeader(value = CustomHeader.USER_ID, required = false) String userId,
                                            @RequestBody PaymentCreateReq req) {
        if (!StringUtils.hasText(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "User ID header is required");
        }
        return paymentService.createPayment(userId, req.getOrderId(), req.getAmount(), req.getCardType(), req.getCardNo());
    }

    /**
     * 주문 생성후 콜백
     * @return
     */
    @PostMapping("/payments/callback")
    public void paymentCallback(@RequestBody TransactionInfo transactionInfo) {
        paymentService.callbackForUpdatePayment(transactionInfo);
    };

    /**
     * 거래번호로 결제 내역 조회
     */
    @GetMapping("/payments/transaction")
    public TransactionDetailResponse getPaymentInfoByTransactionKey(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId
        , @RequestParam("transactionKey") String transactionKey
    ){
        if (!StringUtils.hasText(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "User ID header is required");
        }
        return paymentService.getPaymentInfo(userId, transactionKey);
    }

    /**
     * 주문번호로 거래번호 조회
     * @param userId
     * @param orderId
     * @return
     */
    @GetMapping("/payments/order")
    public OrderResponse getTransactionByOrderNo(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId
        , @RequestParam("orderId") String orderId
    ){
        if (!StringUtils.hasText(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "User ID header is required");
        }
        return paymentService.getTransactionByOrder(userId, orderId);
    }
}
