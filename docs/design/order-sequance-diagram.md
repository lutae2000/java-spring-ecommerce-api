## 주문 생성을 기반으로 분리

```mermaid
sequenceDiagram
    participant Client as 클라이언트
    participant OrderFacade as OrderFacade
    participant OrderService as OrderService
    participant EventBus as EventPublisher
    participant OrderEventHandler as OrderEventHandler
    participant PaymentService as PaymentService
    participant ProductService as ProductService
    participant DataPlatform as 데이터 플랫폼

    Note over Client, DataPlatform: 1. 주문 생성 (메인 트랜잭션)
    Client->>+OrderFacade: 주문 요청
    OrderFacade->>OrderFacade: 입력값 검증
    OrderFacade->>OrderFacade: 사용자/상품 검증
    OrderFacade->>OrderFacade: 포인트 처리
    OrderFacade->>+OrderService: 주문 저장
    OrderService-->>-OrderFacade: 주문 정보 반환
    
    Note over OrderFacade, EventBus: 2. 쿠폰 사용 이벤트 발행 (비동기)
    OrderFacade->>EventBus: CouponUsageEvent 발행
    
    Note over OrderFacade, EventBus: 3. 주문 생성 이벤트 발행 (비동기)
    OrderFacade->>EventBus: OrderCreatedEvent 발행
    OrderFacade-->>-Client: 주문 생성 완료
    
    Note over EventBus, PaymentService: 4. 결제 처리 (별도 트랜잭션, 비동기)
    EventBus->>+OrderEventHandler: OrderCreatedEvent 처리
    OrderEventHandler->>+PaymentService: 결제 요청
    PaymentService-->>-OrderEventHandler: 결제 결과
    OrderEventHandler->>EventBus: OrderPaymentCompletedEvent 발행
    OrderEventHandler-->>-EventBus: 결제 처리 완료
    
    Note over EventBus, ProductService: 5. 재고 차감 및 주문 상태 업데이트 (별도 트랜잭션, 비동기)
    EventBus->>+OrderEventHandler: OrderPaymentCompletedEvent 처리
    alt 결제 성공
        OrderEventHandler->>+ProductService: 재고 차감
        ProductService-->>-OrderEventHandler: 재고 차감 완료
        OrderEventHandler->>+OrderService: 주문 상태 업데이트 (ORDER_PAID)
        OrderService-->>-OrderEventHandler: 상태 업데이트 완료
    else 결제 실패
        OrderEventHandler->>+OrderService: 주문 상태 업데이트 (PAYMENT_FAILED)
        OrderService-->>-OrderEventHandler: 상태 업데이트 완료
    end
    OrderEventHandler->>EventBus: DataPlatformEvent 발행
    OrderEventHandler-->>-EventBus: 후속 처리 완료
    
    Note over EventBus, DataPlatform: 6. 데이터 플랫폼 전송 (외부 I/O, 완전 분리)
    EventBus->>+OrderEventHandler: DataPlatformEvent 처리
    OrderEventHandler->>+DataPlatform: 주문/결제 데이터 전송
    DataPlatform-->>-OrderEventHandler: 전송 완료
    OrderEventHandler-->>-EventBus: 데이터 플랫폼 전송 완료