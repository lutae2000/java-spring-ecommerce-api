package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointRepositoryImpl implements PointRepository {
    private final PointJPARepository pointJPARepository;

    @Override
    public void save(PointEntity pointEntity) {
        pointJPARepository.save(pointEntity);
    }

    @Override
    public Optional<PointEntity> findByLoginId(String loginId) {
        return Optional.ofNullable(pointJPARepository.findByLoginId(loginId));
    }
}
