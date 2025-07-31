package com.loopers.interfaces.api.user;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;


public class UserDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignUpRequest{
        private String userId;
        private String email;
        private String birthday;
        private Gender gender;

        public User toUserEntity() {
            return User.builder()
                .userId(this.userId)
                .email(this.email)
                .birthday(this.birthday)
                .gender(this.gender)
                .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{

        private Long id;
        private String loginId;
        private String email;
        private String birthday;
        private Gender gender;

        public static Response from(UserInfo userInfo){
            return new Response(userInfo.getId(), userInfo.getUserId(), userInfo.getEmail(), userInfo.getBirthday(), userInfo.getGender());
        }
    }

}
