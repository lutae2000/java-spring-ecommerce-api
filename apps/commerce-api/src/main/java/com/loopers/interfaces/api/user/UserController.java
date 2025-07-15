package com.loopers.interfaces.api.user;


import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @GetMapping("/{userId}")
    public ApiResponse<UserDto> getUserInfo(@PathVariable("userId") String userId) {

        log.debug("::: inquiry loginId ::: {}", userId);

         UserDto userDto = userService.getUserInfo(userId);

        return ApiResponse.success(userDto);
    }

    /**
     * 회원가입
     * @param User
     * @return
     */
    @PostMapping("")
    public ApiResponse<UserDto> createUser(@RequestBody UserDto user) {

        log.debug("::: Creating user with login Object ::: {}", user);

        UserDto userDto = userService.createUserId(user);
        return ApiResponse.success(userDto);
    }
}
