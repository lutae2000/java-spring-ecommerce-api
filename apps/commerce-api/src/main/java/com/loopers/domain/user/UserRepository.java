package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<User> selectUserByUserId(String userId);

    User save(User user);
}
