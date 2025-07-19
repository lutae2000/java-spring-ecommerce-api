package com.loopers.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.user.UserDto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    UserService userService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("사용자 생성시")
    @Nested
    class Create{

        String loginId = "login";
        String email = "lutae2000@gmail.com";
        String birthday = "2000-01-01";
        Gender gender = Gender.M;

        @DisplayName("정상 - 사용자 생성")
        @Test
        void createUser_whenSucceed(){

            //given
            UserCommand.Create command = new UserCommand.Create(loginId, email, birthday, gender);

            //when
            UserInfo user = userService.createUserId(command);

            //then
            assertAll(
                () -> assertThat(user.getLoginId()).isEqualTo(loginId),
                () -> assertThat(user.getBirthday()).isEqualTo(birthday),
                () -> assertThat(user.getEmail()).isEqualTo(email),
                () -> assertThat(user.getGender()).isEqualTo(gender)
            );
        }

        @DisplayName("실패 - loginId가 null")
        @Test
        void createUser_when_Failed_loginId_null(){
            //given

            //when
            CoreException response = assertThrows(CoreException.class, () -> {

                //given
                UserCommand.Create command = new UserCommand.Create(null, email, birthday, gender);

                UserInfo user = userService.createUserId(command);
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("실패 - loginId 유효하지 않는 문자열포함")
        @Test
        void createUser_when_failed_loginId_included_invalid_char(){

            //when

            CoreException response = assertThrows(CoreException.class, () -> {
                //given
                UserCommand.Create command = new UserCommand.Create("asdas테스트", email, birthday, gender);

                UserInfo user = userService.createUserId(command);
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("실패 - 이미 가입된 회원ID로 시도")
        @Test
        void createUser_when_failed_already_exists_loginID(){
            //given
            UserCommand.Create command1 = new UserCommand.Create("utlee", email, birthday, gender);

            userService.createUserId(command1);

            //when
            UserCommand.Create command2 = new UserCommand.Create("utlee", email, birthday, gender);

            //then: flush 시점에 예외 발생
            assertThrows(CoreException.class, () -> {
                userService.createUserId(command2);
            });
        }
    }

    @DisplayName("사용자 조회")
    @Nested
    class Find{

        String loginId = "login";
        String email = "lutae2000@gmail.com";
        String birthday = "2000-01-01";
        Gender gender = Gender.M;

        @DisplayName("성공 - 저장된 회원 조회")
        @Test
        void getUserInfo_when_succeed(){
            //given
            UserCommand.Create command = new UserCommand.Create("utlee", email, birthday, gender);

            UserInfo savedUser = userService.createUserId(command);

            //when
            UserInfo result = userService.getUserInfo("utlee");

            //then
            assertAll(
                () -> assertThat(result.getLoginId()).isEqualTo("utlee"),
                () -> assertThat(result.getGender()).isEqualTo(gender),
                () -> assertThat(result.getEmail()).isEqualTo(email),
                () -> assertThat(result.getBirthday()).isEqualTo(birthday)
            );

        }

        @DisplayName("실패 - 조회했으나 조회결과가 없음")
        @ParameterizedTest
        @ValueSource(strings = {"not_exists_loginId1", "not_exists_loginId2"})
        void getUserInfo_when_failed_not_exists_user(String loginIdValue){

            //given
            UserCommand.Create command = new UserCommand.Create("utlee", email, birthday, gender);

            UserInfo savedUser = userService.createUserId(command);

            //when


            //then
            CoreException response = assertThrows(CoreException.class, () -> {
                userService.getUserInfo(loginIdValue);
            });
        }
    }

}
