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
    public UserInfo getUserInfo(String userId) {
        log.debug("::: inquiry userId ::: userId: {}", userId);

        Optional<User> user = userRepository.selectUserByUserId(userId);

        if(user.isPresent()){
            return UserInfo.from(user.get());
        }

        throw new CoreException(ErrorType.NOT_FOUND, "존재하는 회원이 없습니다");
    }

    /**
     * 회원가입
     * @param user
     * @return UserDto
     */
    public UserInfo createUserId(UserCommand.Create userCommand) {

        User user = userCommand.toUserEntity();

        log.debug("::: Creating user with login Object ::: user: {}", user);

        Optional<User> result = userRepository.selectUserByUserId(user.getUserId());

        if(result.isPresent()){   //이미 회원가입 정보 존재여부 확인
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 회원ID 입니다");
        }

        User res = userRepository.save(user);

        return UserInfo.from(user);
    }
}
