package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointRepositoryImpl implements PointRepository {
    private final PointJPARepository pointJPARepository;

    @Override
    public void save(Point Point) {
        pointJPARepository.save(Point);
    }

    @Override
    public Optional<Point> findByUserId(String userId) {
        return pointJPARepository.findByUserId(userId);
    }

    @Override
    public void updatePoint(String userId, Long point) {
        pointJPARepository.updatePoint(userId, point);
    }

}
