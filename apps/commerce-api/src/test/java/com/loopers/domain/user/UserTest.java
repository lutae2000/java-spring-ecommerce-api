package com.loopers.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class UserTest {

    @DisplayName("User 객체 생성시")
    @Nested
    class Create {

        //재사용을 위해 given을 위로
        String loginId = "loginId";
        String email = "lutae2000@gmail.com";
        String birthday = "2000-01-01";
        String gender = Gender.M.name();

        @DisplayName("성공")
        @Test
        void createUser_whenValidSucceed() {
            //given

            //when
                User user = new User(loginId, email, birthday, Gender.valueOf(gender));
            //then
            assertAll(
                () -> assertThat(user.getLoginId()).isNotNull(),
                () -> assertThat(user.getBirthday()).isEqualTo(birthday),
                () -> assertThat(user.getGender()).isEqualTo(Gender.valueOf(gender)),
                () -> assertThat(user.getEmail()).isEqualTo(email)
            );
        }

        @DisplayName("실패 - loginId 누락")
        @Test
        void createUser_when_loginId_null(){
            //given

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                User user = new User(null, email, birthday, Gender.valueOf(gender));
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("실패 - loginId 10자리 초과")
        @Test
        void createUser_when_loginId_over_10_char(){
            //given

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                User user = new User("123456789012345", email, birthday, Gender.valueOf(gender));
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("실패 - loginId 유효하지 않은 문자열")
        @Test
        void createUser_when_loginId_invalid_char(){
            //given

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                User user = new User("로그인 ID", email, birthday, Gender.valueOf(gender));
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }


        @DisplayName("실패 - 생년월일 null")
        @Test
        void createUser_when_birthday_null(){
            //given

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                User user = new User(loginId, email, null, Gender.valueOf(gender));
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }


        @DisplayName("실패 - 잘못된 형식의 생년월일")
        @Test
        void createUser_when_invalid_birthday_format(){
            //given

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                User user = new User(loginId, email, "20000132", Gender.valueOf(gender));
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
