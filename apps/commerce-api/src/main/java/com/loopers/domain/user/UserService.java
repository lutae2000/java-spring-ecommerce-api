package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 유저정보 있는지 조회
     * @param userId
     * @return UserDto
     */
    @Transactional(readOnly = true)
    public UserInfo getUserInfo(String userId) {
        log.debug("::: inquiry userId ::: userId: {}", userId);

        User user = userRepository.selectUserByUserId(userId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하는 회원이 없습니다"));

        return UserInfo.from(user);
    }

    /**
     * 회원가입
     * @param user
     * @return UserDto
     */
    @Transactional
    public UserInfo createUserId(UserCommand.Create userCommand) {

        User user = userCommand.toUserEntity();

        log.debug("::: Creating user with login Object ::: user: {}", user);

        //중복체크
        userRepository.selectUserByUserId(user.getUserId())
            .ifPresent( result -> {
                throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 회원ID 입니다");
            });

        User savedUser = userRepository.save(user);

        return UserInfo.from(savedUser);
    }
}
