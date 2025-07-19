package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {

    void save(String loginId, Long point);

    Optional<PointEntity> findByLoginId(String loginId);
}
