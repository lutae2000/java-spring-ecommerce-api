package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<User> selectUserByLoginId(String loginId);

    User save(User user);

}
