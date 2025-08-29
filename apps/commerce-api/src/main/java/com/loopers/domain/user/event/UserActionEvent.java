package com.loopers.domain.user.event;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Table(name = "user_action")
@Entity
@Getter
@NoArgsConstructor
public class UserActionEvent extends BaseEntity {
    private String userId;
    private String action;
    private String actionId;

    public UserActionEvent(String userId, String action, String actionId) {
        this.userId = userId;
        this.action = action;
        this.actionId = actionId;
    }
}
