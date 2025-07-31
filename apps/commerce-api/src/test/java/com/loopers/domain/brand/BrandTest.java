package com.loopers.domain.brand;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.loopers.domain.point.PointEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class BrandTest {
    @DisplayName("성공 - 브랜드 객체 생성")
    @ParameterizedTest
    @CsvSource({
        "AA, 브랜드 AA, 브랜드 AA 설명,  img_url, true",
        "BB, 브랜드 BB, 브랜드 BB 설명, null, true",
        "CC, 브랜드 CC, 브랜드 CC 설명, img_url, false"
    })
    void brandCreate(String code, String name, String description, String imgUrl, Boolean useYn){

        //given

        //when
        Brand brand = new Brand(code, name, description, imgUrl, useYn);

        //then
        assert brand.getCode().equals(code);
        assert brand.getName().equals(name);
        assert brand.getDescription().equals(description);
    }

    @DisplayName("실패 - 브랜드 제목 누락시 400에러")
    @ParameterizedTest
    @CsvSource({
        "AA, , 브랜드 AA 설명,  img_url, true",
        "BB, , 브랜드 BB 설명, , true",
        "CC, , 브랜드 CC 설명, img_url, false"
    })
    void brandName_null_fail(String code, String name, String description, String imgUrl, Boolean useYn){

        //when
        CoreException response = assertThrows(CoreException.class, () -> {
            Brand brand = new Brand(code, name, description, imgUrl, useYn);
        });

        //then
        assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("실패 - 브랜드 설명 누락시 400에러")
    @ParameterizedTest
    @CsvSource({
        "AA, 나이키, ,  img_url, true",
        "BB, hoka, , , true",
        "CC, 아디다스, , img_url, false"
    })
    void brandDesc_null_fail(String code, String name, String description, String imgUrl, Boolean useYn){

        //when
        CoreException response = assertThrows(CoreException.class, () -> {
            Brand brand = new Brand(code, name, description, imgUrl, useYn);
        });

        //then
        assertThat(response.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }
}
