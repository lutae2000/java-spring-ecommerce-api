package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {

    void save(Point Point);

    Optional<Point> findByUserId(String userId);

    void updatePoint(String userId, Long point);
}
