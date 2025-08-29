package com.loopers.domain.user.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActionEventListener {
    private final UserActionLogRepository userActionLogRepository;

    @Async
    @EventListener
    public void handleUserAction(UserActionEvent event) {

        userActionLogRepository.saveUserActionLog(event);
    }
}
