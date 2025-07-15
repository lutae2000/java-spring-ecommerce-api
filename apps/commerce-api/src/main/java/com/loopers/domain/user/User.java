package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.domainEnum.Gender;
import com.loopers.interfaces.api.user.UserDto;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name="user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class User extends BaseEntity {

    private String loginId;

    private String email;

    private String birthday;

    private String gender;


    public UserDto toUserDto() {
        return UserDto.builder()
            .loginId(this.loginId)
            .email(this.email)
            .birthday(this.birthday)
            .gender(Gender.valueOf(this.gender))
            .build();
    }
}
