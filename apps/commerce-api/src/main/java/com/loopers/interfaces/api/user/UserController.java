package com.loopers.interfaces.api.user;


import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;

import com.loopers.support.annotation.RequireLoginHeader;
import com.loopers.support.header.CustomHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    /**
     * 회원조회
     * @param loginId
     * @return
     */
    @GetMapping("/me")
    @RequireLoginHeader
    public ApiResponse<UserDto.Response> getUserInfo(
        @RequestHeader(value = CustomHeader.USER_ID, required = false) String userId
    ) {

        log.debug("::: inquiry loginId ::: {}", userId);

         UserInfo userInfo = userService.getUserInfo(userId);

        return ApiResponse.success(UserDto.Response.from(userInfo));
    }

    /**
     * 회원가입
     * @param User
     * @return
     */
    @PostMapping("")
    public ApiResponse<UserDto.Response> createUser(
        @RequestBody UserDto.SignUpRequest user
    ) {

        log.debug("::: Creating user with login Object ::: {}", user);

        UserCommand.Create command = new UserCommand.Create(user.getLoginId(), user.getEmail(), user.getBirthday(), user.getGender());

        UserInfo userInfo = userService.createUserId(command);
        return ApiResponse.success(UserDto.Response.from(userInfo));
    }
}
