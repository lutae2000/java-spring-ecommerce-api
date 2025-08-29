package com.loopers.domain.like.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LikeEvent extends ApplicationEvent {
    private final String userId;
    private final String productId;
    private final boolean isIncrement;

    public LikeEvent(Object source, String userId, String productId, boolean isIncrement) {
        super(source);
        this.userId = userId;
        this.productId = productId;
        this.isIncrement = isIncrement;
    }
}
