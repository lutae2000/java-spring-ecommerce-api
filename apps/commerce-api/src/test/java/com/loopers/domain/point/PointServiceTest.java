package com.loopers.domain.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.point.PointCommand.Create;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PointServiceTest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    PointService pointService;

    @Autowired
    UserService userService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }



    @Nested
    @DisplayName("포인트 조회")
    class GetPointInfo {

        String loginId = "utlee";
        String email = "lutae2000@gmail.com";
        String birthday = "2000-01-01";
        Gender gender = Gender.M;
        Long pointChargeValue = 10000L;


        @Test
        @DisplayName("성공")
        void inquiryPoint(){
            //given
            UserCommand.Create command = new UserCommand.Create(loginId, email, birthday, gender);

            //when
            UserInfo user = userService.createUserId(command);

            PointInfo pointInfo =  pointService.getPointInfo(user.getLoginId());

            //then
            assertAll(
                () -> assertThat(pointInfo.getLoginId()).isEqualTo("utlee"),
                () -> assertThat(pointInfo.getPoint()).isNotNull()
            );
        }

        @Test
        @DisplayName("실패 - 회원 미존재")
        void inquiryPoint_when_not_exists_user(){

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                PointInfo pointInfo = pointService.getPointInfo("not_exists_loginId");
            });

            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);

        }


    }

    @Nested
    @DisplayName("포인트 충전")
    class pointCharge {

        String loginId = "utlee";
        Long pointChargeValue = 10000L;
        String email = "lutae2000@gmail.com";
        String birthday = "2000-01-01";
        Gender gender = Gender.M;

        @DisplayName("성공")
        @Test
        void chargePointSucceed(){
            //given
            PointCommand.Create pointCommand = new PointCommand.Create(loginId, pointChargeValue);

            //given
            UserCommand.Create command = new UserCommand.Create(loginId, email, birthday, gender);
            UserInfo user = userService.createUserId(command);

            //when
            PointInfo pointInfo = pointService.chargePoint(pointCommand);

            //then
            assertAll(
                () -> assertThat(pointInfo.getLoginId()).isEqualTo("utlee"),
                () -> assertThat(pointInfo.getPoint()).isNotNull()
            );

        }

        @DisplayName("실패 - 회원정보 없음")
        @Test
        void chargePointFail(){

            //given
            PointCommand.Create pointCommand = new Create("utlee", 0L);

            //when
            CoreException response = assertThrows(CoreException.class, () -> {
                PointInfo pointInfo = pointService.chargePoint(pointCommand);
            });


            //then
            assertThat(response.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }



    }
}
