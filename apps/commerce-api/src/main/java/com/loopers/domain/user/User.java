package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.domainEnum.Gender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;

@Getter
@Table(name="user")
@NoArgsConstructor
@Entity
public class User extends BaseEntity {

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    private String email;

    private String birthday;

    private Gender gender;

    @Builder
    public User(String loginId, String email, String birthday, Gender gender) {
        loginIdValid(loginId);
        emailValid(email);
        birthdayValid(birthday);

        this.loginId = loginId;
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;
    }

    private void loginIdValid(String loginId){
        if(StringUtils.isEmpty(loginId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "loginId 값은 필수입니다");
        }
        if(!loginId.matches("^[a-zA-Z0-9]{1,10}$")){
            throw new CoreException(ErrorType.BAD_REQUEST, "loginId는 영문 대/소문자와 숫자만 가능하며 10자 이내만 가능합니다");
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

}
