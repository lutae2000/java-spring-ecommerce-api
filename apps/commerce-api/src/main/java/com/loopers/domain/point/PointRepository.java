package com.loopers.domain.point;

import java.util.List;

public interface PointRepository {

    void save(String loginId, Long point);

    PointEntity findByLoginId(String loginId);
}
