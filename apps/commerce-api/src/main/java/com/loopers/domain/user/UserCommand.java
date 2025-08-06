package com.loopers.domain.user;

import com.loopers.domain.domainEnum.Gender;

public class UserCommand {

    private String loginId;
    private String email;
    private String birthday;
    private Gender gender;

    public record Create(String loginId, String email, String birthday, Gender gender) {

        public User toUserEntity() {
            return User.builder()
                .userId(this.loginId)
                .email(this.email)
                .birthday(this.birthday)
                .gender(this.gender)
                .build();
        }
    }

}
