package com.loopers.domain.domainEnum;

public enum OrderStatus {
    ORDER_PLACED,        // 주문 접수
    PAYMENT_FAILED,      // 결제 실패
    ORDER_PAID,          // 주문 결제됨
    ORDER_CONFIRMED,     // 주문 확인
    ORDER_REFUND,        // 주문 환불
    ORDER_CANCEL,        // 주문 취소
    ORDER_SHIP,          // 배송 중
    ORDER_COMPLETE;      // 주문 완료
}
