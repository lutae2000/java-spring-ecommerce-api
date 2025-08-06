package com.loopers.domain.point;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


public class PointTest {

    @DisplayName("성공 - 포인트 충전")
    @ParameterizedTest
    @CsvSource({
        "utlee, 10000",
        "test, 1000000",
        "player, 10000000"
    })
    void pointCharge(String loginId, Long pointValue){

        //given

        //when
        PointEntity pointModel = new PointEntity(loginId, pointValue);

        //then
        assert pointModel.getUserId().equals(loginId);
        assert pointModel.getPoint().equals(pointValue);
    }

    @DisplayName("실패 - 0또는 마이너스 포인트 충전시 400에러 발생")
    @ParameterizedTest
    @CsvSource({
        "chicken, 0",
        "utlee, -10000",
        "test, -1000000",
        "player, -10000000"
    })
    void pointChargeFail(String loginId, Long pointValue){

        //when
        CoreException response = assertThrows(CoreException.class, () -> {
            PointEntity pointModel = new PointEntity(loginId, pointValue);
        });

        //then
        assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }
}
