package com.loopers.interfaces.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointCommand.Create;
import com.loopers.interfaces.api.point.PointDto;
import com.loopers.interfaces.api.user.UserDto;
import com.loopers.interfaces.api.user.UserDto.Response;
import com.loopers.support.header.CustomHeader;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointV1ApiE2ETest {

    private static final String ENDPOINT = "/api/v1/points";
    private static final String ENDPOINT_SIGNUP = "/api/v1/users";
    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public PointV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
    }


    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @DisplayName("POST /api/v1/points/charge")
    @Nested
    class accumulatePoint {


        String loginId = "utlee";
        Long pointChargeValue = 10000L;
        String email = "lutae2000@gmail.com";
        String birthday = "2000-01-01";
        Gender gender = Gender.M;


        @DisplayName("성공 - 정상케이스")
        @Test
        void accumulatePointSuccess(){

            /**
             * given
             * given 회원 생성
             */
            UserDto.SignUpRequest request1 = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email(email)
                .gender(Gender.M)
                .userId(loginId)
                .build();

            ResponseEntity<ApiResponse<UserDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT_SIGNUP,
                HttpMethod.POST,
                new HttpEntity<>(request1),
                new ParameterizedTypeReference<>() {});

                PointCommand.Create command = new Create(loginId, 1000L);

                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add("X-USER-ID", loginId);

                ResponseEntity<ApiResponse<PointDto.Response>> res = testRestTemplate.exchange(
                    ENDPOINT + "/charge",
                    HttpMethod.POST,
                    new HttpEntity<>(command, httpHeaders),
                    new ParameterizedTypeReference<>() {});

            assertAll(
                () -> assertTrue(res.getStatusCode().is2xxSuccessful()),
                () -> assertTrue(res.getBody().data().getPoint() == 1000L)
            );
        }

        @DisplayName("성공 - 포인트를 여러건 충전시 누적된 값이 응답으로 와야함")
        @Test
        void accumulatePointSuccess_lotsOfTimesPointChargeValue(){
            Long[] chargeAmounts = {10000L, 20000L};    //중전할 포인트
            Long[] expectedResults = {10000L, 30000L};  //예상 누적 포인트
            /**
             * given
             * given 회원 생성
             */
            UserDto.SignUpRequest request1 = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email(email)
                .gender(Gender.M)
                .userId(loginId)
                .build();

            ResponseEntity<ApiResponse<UserDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT_SIGNUP,
                HttpMethod.POST,
                new HttpEntity<>(request1),
                new ParameterizedTypeReference<>() {});

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", loginId);

            for(int i = 0; i < chargeAmounts.length; i++){
                PointCommand.Create command = new Create(loginId, chargeAmounts[i]);

                ResponseEntity<ApiResponse<PointDto.Response>> res = testRestTemplate.exchange(
                    ENDPOINT + "/charge",
                    HttpMethod.POST,
                    new HttpEntity<>(command, httpHeaders),
                    new ParameterizedTypeReference<>() {});

                int finalI = i;
                assertAll(
                    () -> assertTrue(res.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(res.getBody().data().getPoint()).isEqualTo(expectedResults[finalI])
                );
            }
        }

        @DisplayName("실패 - 회원정보 없을때 404에러 발생")
        @Test
        void accumulatePointFailed_notExistsUser(){
            PointCommand.Create command = new Create(loginId, 1000L);


            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", loginId);

            ResponseEntity<ApiResponse<Response>> res = testRestTemplate.exchange(
                ENDPOINT + "/charge",
                HttpMethod.POST,
                new HttpEntity<>(command, httpHeaders),
                new ParameterizedTypeReference<>() {});

            assertAll(
                () -> assertTrue(res.getStatusCode().is4xxClientError()),
                () -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }

        @DisplayName("실패 - 포인트 마이너스 적립시도하여 400에러 발생")
        @Test
        void accumulatePointFailed_minusPointChargeValue(){
            /**
             * given
             * given 회원 생성
             */
            UserDto.SignUpRequest request1 = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email(email)
                .gender(Gender.M)
                .userId(loginId)
                .build();

            ResponseEntity<ApiResponse<UserDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT_SIGNUP,
                HttpMethod.POST,
                new HttpEntity<>(request1),
                new ParameterizedTypeReference<>() {});

            PointCommand.Create command = new Create(loginId, -1000L);


            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", loginId);

            ResponseEntity<ApiResponse<Response>> res = testRestTemplate.exchange(
                ENDPOINT + "/charge",
                HttpMethod.POST,
                new HttpEntity<>(command, httpHeaders),
                new ParameterizedTypeReference<>() {});

            assertAll(
                () -> assertTrue(res.getStatusCode().is4xxClientError())
            );
        }

    }

    @Nested
    @DisplayName("GET /api/v1/points")
    class getPoint {

        String loginId = "utlee";

        String email = "lutae2000@gmail.com";
        String birthday = "2000-01-01";

        @DisplayName("성공 - 정상 케이스")
        @Test
        void getPointSuccess(){

            //회원 생성
            UserDto.SignUpRequest request1 = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email(email)
                .gender(Gender.M)
                .userId(loginId)
                .build();

            ResponseEntity<ApiResponse<UserDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT_SIGNUP,
                HttpMethod.POST,
                new HttpEntity<>(request1),
                new ParameterizedTypeReference<>() {});

            PointCommand.Create command = new Create(loginId, 1000L);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(CustomHeader.USER_ID, loginId);

            ResponseEntity<ApiResponse<Response>> res = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(command, httpHeaders),
                new ParameterizedTypeReference<>() {});


            assertAll(
                () -> assertTrue(res.getStatusCode().is2xxSuccessful())
            );
        }


        @DisplayName("실패 - 헤더누락으로 400에러 발생")
        @Test
        void getPointFailed_headerMissing(){

            UserDto.SignUpRequest request1 = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email(email)
                .gender(Gender.M)
                .userId(loginId)
                .build();

            ResponseEntity<ApiResponse<UserDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT_SIGNUP,
                HttpMethod.POST,
                new HttpEntity<>(request1),
                new ParameterizedTypeReference<>() {});

            PointCommand.Create command = new Create(loginId, 1000L);



            ResponseEntity<ApiResponse<Response>> res = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(command, null),
                new ParameterizedTypeReference<>() {});

            assertAll(
                () -> assertTrue(res.getStatusCode().is4xxClientError())
            );
        }

        @DisplayName("실패 - 없는 회원으로 충전요청시 404에러 발생")
        @Test
        void getPointFailed_not_exists(){
            /**
             * given
             * given 회원 생성
             */
            UserDto.SignUpRequest request1 = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email(email)
                .gender(Gender.M)
                .userId(loginId)
                .build();

            ResponseEntity<ApiResponse<UserDto.Response>> response = testRestTemplate.exchange(
                ENDPOINT_SIGNUP,
                HttpMethod.POST,
                new HttpEntity<>(request1),
                new ParameterizedTypeReference<>() {});

            PointCommand.Create command = new Create(loginId+"1", 1000L);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(CustomHeader.USER_ID, loginId+"1");

            ResponseEntity<ApiResponse<Response>> res = testRestTemplate.exchange(
                ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(command, httpHeaders),
                new ParameterizedTypeReference<>() {});

            assertAll(
                () -> assertTrue(res.getStatusCode().is4xxClientError())
            );
        }
    }

}
