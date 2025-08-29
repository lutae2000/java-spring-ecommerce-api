package com.loopers.infrastructure.user.event;

import com.loopers.domain.user.event.UserActionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActionLogJpaRepository extends JpaRepository<UserActionEvent, String> {
    
}
