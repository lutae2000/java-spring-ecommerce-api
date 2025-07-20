package com.loopers.interfaces.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.loopers.domain.domainEnum.Gender;
import com.loopers.interfaces.api.user.UserDto;
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
public class UserV1ApiE2ETest {

    private static final String ENDPOINT_SIGNUP = "/api/v1/users";

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public UserV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/users")
    @Nested
    class SignUp {

        String loginId = "utlee";
        String email = "utlee@naver.com";
        String birthday = "2000-01-01";
        String gender = "M";

        @DisplayName("성공 - 정상 케이스")
        @Test
        void createUser(){

            UserDto.SignUpRequest request = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email(email)
                .gender(Gender.valueOf(gender))
                .loginId(loginId)
                .build();

            String requestURL =  ENDPOINT_SIGNUP;

            ResponseEntity<ApiResponse<UserDto.Response>> response = testRestTemplate.exchange(
                                                                requestURL,
                                                                HttpMethod.POST,
                                                                new HttpEntity<>(request),
                                                                new ParameterizedTypeReference<>() {});

            assertAll(
                ()  -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertNotNull(response.getBody().data().getId())
            );

        }

        @DisplayName("실패 - 이미 생성된 유저로 가입 시도시 400에러")
        @Test
        void createUserDuplicated(){

            String requestURL =  ENDPOINT_SIGNUP;

            UserDto.SignUpRequest request1 = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email(email)
                .gender(Gender.valueOf(gender))
                .loginId(loginId)
                .build();

            //최초 생성
            ResponseEntity<ApiResponse<UserDto.Response>> response = testRestTemplate.exchange(
                requestURL,
                HttpMethod.POST,
                new HttpEntity<>(request1),
                new ParameterizedTypeReference<>() {});


            UserDto.SignUpRequest request2 = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email(email)
                .gender(Gender.valueOf(gender))
                .loginId(loginId)
                .build();

            ResponseEntity<ApiResponse<UserDto.Response>> response2 = testRestTemplate.exchange(
                requestURL,
                HttpMethod.POST,
                new HttpEntity<>(request2),
                new ParameterizedTypeReference<>() {});

            assertAll(
                () -> assertTrue(response2.getStatusCode().is4xxClientError())
            );
        }

        @DisplayName("실패 - 잘못된 이메일 형식으로 가입 시도시 400에러")
        @Test
        void createUser_invalid_email(){

            String requestURL =  ENDPOINT_SIGNUP;

            UserDto.SignUpRequest request1 = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email("123@com")
                .gender(Gender.valueOf(gender))
                .loginId(loginId)
                .build();

            //최초 생성
            ResponseEntity<ApiResponse<UserDto.Response>> response = testRestTemplate.exchange(
                requestURL,
                HttpMethod.POST,
                new HttpEntity<>(request1),
                new ParameterizedTypeReference<>() {});

            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError())
            );
        }

        @DisplayName("실패 - 성별 선택없이 회원가입시 400에러")
        @Test
        void createUser_non_gender(){

            String requestURL =  ENDPOINT_SIGNUP;

            UserDto.SignUpRequest request1 = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email(email)
                .gender(null)
                .loginId(loginId)
                .build();

            //최초 생성
            ResponseEntity<ApiResponse<UserDto.Response>> response = testRestTemplate.exchange(
                requestURL,
                HttpMethod.POST,
                new HttpEntity<>(request1),
                new ParameterizedTypeReference<>() {});

            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError())
            );
        }
    }

    @DisplayName("GET /api/v1/users/{loginId}")
    @Nested
    class Get {

        String loginId = "utlee";
        String email = "utlee@naver.com";
        String birthday = "2000-01-01";
        String gender = "M";
        String requestURL =  ENDPOINT_SIGNUP;


        @DisplayName("정상 - 존재하는 회원 조회")
        @Test
        void inquiryUser(){

            //given 회원 생성
            UserDto.SignUpRequest request1 = UserDto.SignUpRequest.builder()
                .birthday(birthday)
                .email(email)
                .gender(Gender.valueOf(gender))
                .loginId(loginId)
                .build();

            //최초 생성
            ResponseEntity<ApiResponse<UserDto.Response>> response = testRestTemplate.exchange(
                requestURL,
                HttpMethod.POST,
                new HttpEntity<>(request1),
                new ParameterizedTypeReference<>() {});



            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", loginId);

            ResponseEntity<ApiResponse<UserDto.Response>> response1 = testRestTemplate.exchange(
                ENDPOINT_SIGNUP + "/me",
                HttpMethod.GET,
                new HttpEntity<>(null, httpHeaders),
                new ParameterizedTypeReference<>(){}
            );

            //then
            assertAll(
                () -> assertTrue(response1.getStatusCode().is2xxSuccessful()),
                () -> assertNotNull(response1.getBody().data().getId()),
                () -> assertThat(response1.getBody().data().getLoginId()).isEqualTo(loginId),
                () -> assertThat(response1.getBody().data().getEmail()).isEqualTo(email),
                () -> assertThat(response1.getBody().data().getBirthday()).isEqualTo(birthday),
                () -> assertThat(response1.getBody().data().getGender().name()).isEqualTo(gender)
            );
        }


        @DisplayName("실패 - 미존재 회원이라면 404 상태응답 & null 응답")
        @Test
        void inquiryNotExistsUser(){
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-USER-ID", loginId);

            ResponseEntity<ApiResponse<UserDto.Response>> response1 = testRestTemplate.exchange(
                ENDPOINT_SIGNUP + "/me/notExistsUser",
                HttpMethod.GET,
                new HttpEntity<>(null, httpHeaders),
                new ParameterizedTypeReference<>(){}
            );

            //then
            assertAll(
                () -> assertTrue(response1.getStatusCode().is4xxClientError()),
                () -> assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }

    }
}
