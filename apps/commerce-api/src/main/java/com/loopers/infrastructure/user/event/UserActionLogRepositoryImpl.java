package com.loopers.infrastructure.user.event;

import com.loopers.domain.user.event.UserActionEvent;
import com.loopers.domain.user.event.UserActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserActionLogRepositoryImpl implements UserActionLogRepository {
    private final UserActionLogJpaRepository userActionLogJpaRepository;

    /**
     * 사용자 행동 로그 저장
     * @param userActionEvent
     * @return
     */
    @Override
    public UserActionEvent saveUserActionLog(UserActionEvent userActionEvent) {
        return userActionLogJpaRepository.save(userActionEvent);
    }
}
