package com.loopers.domain.brand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)  // 인스턴스 재사용
public class BrandServiceTest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    BrandService brandService;

    @BeforeAll
    void setup() {
        // 공통 데이터 삽입 (테스트 전체에서 사용)
        Brand initerBrand = new Brand("nike", "nike", "description1", "imgURL1", true);
        brandService.createBrand(new BrandCommand.Create(initerBrand.getCode(), initerBrand.getName(), initerBrand.getDescription(), initerBrand.getImgURL(), initerBrand.getUseYn() ) );
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("브랜드 생성")
    class CreateBrand {

        @DisplayName("브랜드 생성 - 성공")
        @ParameterizedTest
        @CsvSource({
            "code1, name1, description1, imgURL1, true",
            "code2, name2, description2, imgURL2, false",
            "code3, name3, description3, null, true",
            "code4, name4, description4, null, false",
        })
        @Order(1)
        void createBrand_whenSucceed(String code, String name, String description, String imgURL, boolean useYn){

            BrandCommand.Create param = new BrandCommand.Create(code, name, description, imgURL, useYn);

            BrandInfo result = brandService.createBrand(param);

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getCode()).isEqualTo(code),
                () -> assertThat(result.getName()).isEqualTo(name),
                () -> assertThat(result.getDescription()).isEqualTo(description),
                () -> assertThat(result.getImgURL()).isEqualTo(imgURL),
                () -> assertThat(result.getUseYn()).isEqualTo(useYn)
            );
        }

        @DisplayName("실패 - 이미 존재하는 브랜드를 등록요청")
        @ParameterizedTest
        @CsvSource({
            "nike, nike, description3, null, true",
        })
        @Order(2)
        void createBrand_whenFailed(String code, String name, String description, String imgURL, boolean useYn) {

            BrandCommand.Create param = new BrandCommand.Create(code, name, description, imgURL, useYn);

            CoreException response = Assert.assertThrows(CoreException.class, () -> {
                brandService.createBrand(param);
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }
    }

    @Nested
    @DisplayName("브랜드 조회")
    class SearchBrand {

        @DisplayName("성공")
        @ParameterizedTest
        @CsvSource({
            "nike"
        })
        @Order(3)
        void inquiryBrand_whenSucceed(String code){


            BrandInfo result = brandService.findByBrandCode(code);

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getCode()).isEqualTo(code)
            );
        }

        @DisplayName("실패 - 400에러 / NOT_FOUND 발생")
        @Order(4)
        @ParameterizedTest
        @CsvSource({
            "adidas",
            "hoka"
        })
        void inquiryBrand_when_Failed_400(String code){
            CoreException response = Assert.assertThrows(CoreException.class, () -> {
                BrandInfo result = brandService.findByBrandCode(code);
            });

            assertThat(response.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
