package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {

    void save(PointEntity pointEntity);

    Optional<PointEntity> findByLoginId(String loginId);
}
