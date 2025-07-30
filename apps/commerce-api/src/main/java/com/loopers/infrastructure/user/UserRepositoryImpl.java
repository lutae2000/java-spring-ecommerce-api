package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> selectUserByUserId(String userId) {
        return userJpaRepository.findByLoginId(userId);
    }

    @Override
    public User save(User model) {
        return userJpaRepository.save(model);
    }

}
