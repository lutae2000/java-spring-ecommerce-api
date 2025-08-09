package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.domainEnum.Gender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
@Table(name="user")
@NoArgsConstructor
@Entity
public class User extends BaseEntity {

    @Column(name = "userId", nullable = false, unique = true)
    private String userId;

    private String email;

    private String birthday;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Builder
    public User(String userId, String email, String birthday, Gender gender) {
        userIdValid(userId);
        emailValid(email);
        birthdayValid(birthday);
        genderValid(gender);
        this.userId = userId;
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;
    }

    private void userIdValid(String userId){
        if(StringUtils.isEmpty(userId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더 값은 필수입니다");
        }
        if(!userId.matches("^[a-zA-Z0-9]{1,10}$")){
            throw new CoreException(ErrorType.BAD_REQUEST, "userId는 영문 대/소문자와 숫자만 가능하며 10자 이내만 가능합니다");
        }
    }

    private void emailValid(String email){
        if(StringUtils.isEmpty(email)){
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일값은 null이 불가합니다");
        }

        if(!email.matches("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")){
            throw new CoreException(ErrorType.BAD_REQUEST, "유효한 이메일 형식이 아닙니다");
        }
    }

    private void birthdayValid(String birthday){
        if(StringUtils.isEmpty(birthday)){
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 null이 불가합니다");
        }

        if (!birthday.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 YYYY-MM-DD 형식이어야 합니다");
        }
    }

    private void genderValid(Gender gender){
        if(ObjectUtils.isEmpty(gender)){
            throw new CoreException(ErrorType.BAD_REQUEST, "성별 값은 필수 입니다");
        }
    }

}
