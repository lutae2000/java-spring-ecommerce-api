package com.loopers.interfaces.api.user;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class UserDto {

    @Id
    private String loginId;

    private String email;

    private String birthday;

    private Gender gender;

    public UserDto(String loginId, String email, String birthday, Gender gender) {
        if(StringUtils.isBlank(loginId) || !loginId.matches("^[a-zA-Z0-9]{1,10}$")){
            throw new CoreException(ErrorType.BAD_REQUEST, "loginId는 영문 대/소문자와 숫자만 가능하며 10자 이내만 가능합니다");
        }

        if(StringUtils.isBlank(email) && !email.matches("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")){
            throw new CoreException(ErrorType.BAD_REQUEST, "유효한 이메일 형식이 아닙니다");
        }

        if (StringUtils.isNotBlank(birthday) && !birthday.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 YYYY-MM-DD 형식이어야 합니다");
        }

        if(ObjectUtils.isEmpty(gender)){
            throw new CoreException(ErrorType.BAD_REQUEST, "성별입력은 필수입니다");
        }

        this.loginId = loginId;
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;
    }

    public User toUserEntity() {
        return User.builder()
            .loginId(this.loginId)
            .email(this.email)
            .birthday(this.birthday)
            .gender(String.valueOf(this.gender))
            .build();
    }

}
