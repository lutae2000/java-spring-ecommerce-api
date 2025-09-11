package com.loopers.domain.like.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeChangedEvent {
    private String eventId;
    private String productId;
    private String userId;
    private String action; // "LIKED" or "UNLIKED"
    private Long currentLikesCount;
    private LocalDateTime occurredAt;
    
    public LikeChangedEvent(String productId, String userId, String action, Long currentLikesCount) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.productId = productId;
        this.userId = userId;
        this.action = action;
        this.currentLikesCount = currentLikesCount;
        this.occurredAt = LocalDateTime.now();
    }
}
