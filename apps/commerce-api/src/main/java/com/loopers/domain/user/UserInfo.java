package com.loopers.domain.user;

import com.loopers.domain.domainEnum.Gender;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserInfo {

    private Long id;
    private String loginId;
    private String email;
    private String birthday;
    private Gender gender;

    public static UserInfo from(User user) {
        return UserInfo.builder()
            .id(user.getId())
            .loginId(user.getLoginId())
            .email(user.getEmail())
            .birthday(user.getBirthday())
            .gender(user.getGender())
            .build();
    }
}
