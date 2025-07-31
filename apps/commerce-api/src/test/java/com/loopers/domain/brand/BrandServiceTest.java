package com.loopers.domain.brand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
public class BrandServiceTest {

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @Autowired
    BrandService brandService;


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
    }

    @Nested
    @DisplayName("브랜드 조회")
    class SearchBrand {

        @DisplayName("브랜드 생성 - 성공")
        @ParameterizedTest
        @CsvSource({
            "code1, name1, description1, imgURL1, true",
            "code2, name2, description2, imgURL2, false",
            "code3, name3, description3, null, true",
            "code4, name4, description4, null, false",
        })
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
    }
}
