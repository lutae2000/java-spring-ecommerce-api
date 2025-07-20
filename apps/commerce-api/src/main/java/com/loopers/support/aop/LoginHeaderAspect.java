package com.loopers.support.aop;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;

import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * AOP 선언
 */
@Aspect
@Component
public class LoginHeaderAspect {

    @Before("@annotation(com.loopers.support.annotation.RequireLoginHeader)")
    public void checkLoginHeader(String loginId){
        if(StringUtils.isBlank(loginId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더값은 필수입니다");
        }
    }
}
