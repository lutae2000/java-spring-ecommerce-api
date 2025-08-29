## 좋아요/취소를 이벤트 기반으로 분리

```mermaid
sequenceDiagram
participant Client
participant LikeFacade
participant LikeService
participant DB
participant EventPublisher
participant LikeEventHandler
participant LikeSummaryRepository

    Client->>LikeFacade: like(userId, productId)
    LikeFacade->>LikeService: like(userId, productId)
    LikeService->>DB: 좋아요 생성 (트랜잭션 시작)
    LikeService->>EventPublisher: publishEvent(LikeEvent)
    EventPublisher->>LikeEventHandler: 이벤트 실행
    EventPublisher-->>LikeService: 이벤트 발행 완료
    LikeService-->>LikeFacade: 좋아요 생성 완료
    LikeFacade-->>Client: 응답
    
    Note over DB: 트랜잭션 커밋 
    LikeEventHandler->>LikeSummaryRepository: 상품의 좋아요 횟수 조회
    LikeSummaryRepository-->>LikeEventHandler: 좋아요 횟수응답 
    LikeEventHandler->>LikeSummaryRepository: 좋아요/취소 횟수 업데이트 요청