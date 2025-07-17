package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointRepositoryImp implements PointRepository {
    private final PointJPARepository pointJPARepository;

    @Override
    public void save(String loginId, Long point) {
        pointJPARepository.save(PointEntity.builder().loginId(loginId).point(point).build());
    }

    @Override
    public PointEntity findByLoginId(String loginId) {
        return pointJPARepository.findByLoginId(loginId);
    }
}
