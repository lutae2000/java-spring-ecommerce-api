package com.loopers.domain.user.event;


public interface UserActionLogRepository {

    /**
     * 사용자 행동 로그 저장
     * @param UserActionEvent
     * @return
     */
    UserActionEvent saveUserActionLog(UserActionEvent userActionEvent);
}
