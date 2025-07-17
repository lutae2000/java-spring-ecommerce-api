package com.loopers.domain.point;

public interface PointRepository {

    void save(String loginId, Long point);

    PointEntity findByLoginId(String loginId);
}
